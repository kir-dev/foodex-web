package hu.kirdev.foodex.oidcuser

import hu.kirdev.foodex.admingrant.AdminGrantService
import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.trialgrant.TrialGrantService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class FoodExOidcUserService(
    val userService: UserService,
    val cookingClubService: CookingClubService,
    @param:Qualifier("superuserIds") private val superuserIds: Set<String>,
    private val trialGrantService: TrialGrantService,
    private val adminGrantService: AdminGrantService,
) : OidcUserService() {

    private final val foodExID = 182L

    // Upsert user and reload club leadership on login
    @Transactional(readOnly = false)
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val authschUser = super.loadUser(userRequest)

        val foodexUser = FoodExOidcUser(authschUser)
        val knownClubIds = cookingClubService.getAllCookingClubs().map { it.id }.toSet()
        val leaderAt = cookingClubIdsLedBy(foodexUser, knownClubIds)

        val existing = userService.getUserByInternalId(foodexUser.internalId)
        val role = resolveRole(foodexUser)

        val user = if (existing != null) {
            existing.role = role
            existing.email = foodexUser.requiredEmail
            existing.isActive = role != Role.GUEST
            existing
        } else {
            UserEntity(
                internalId = foodexUser.internalId,
                role = role,
                name = foodexUser.requiredName,
                nickname = foodexUser.nickName,
                email = foodexUser.requiredEmail,
                favouriteQuote = null,
                isActive = role != Role.GUEST,
                profilePicture = foodexUser.profile,
            )
        }

        // Persist first so leadership ops have a real user id
        val saved = userService.updateUser(user)
        reloadPermissionsOfUserToCookingClubs(saved, leaderAt)
        foodexUser.extraAuthorities = authoritiesFor(saved.role)

        return foodexUser
    }

    fun resolveRole(foodexUser: FoodExOidcUser): Role {
        val base = getHighestRole(foodexUser)
        val withTrial = applyTrialGrant(foodexUser.internalId, base)
        return applyAdminGrant(foodexUser.internalId, withTrial)
    }

    fun getHighestRole(foodexUser: FoodExOidcUser): Role {
        if (foodexUser.internalId in superuserIds) {
            return Role.SUPERUSER
        }

        if (foodexUser.executiveAtCircles.any { it.id == foodExID }) {
            return Role.ADMIN
        }

        for (membership in foodexUser.memberships) {
            if (membership.id == foodExID) {
                if (membership.title.any { it.contains("újonc", ignoreCase = true) }) {
                    return Role.NEWBIE
                }
                return Role.MEMBER
            }
        }

        return Role.GUEST
    }

    fun applyTrialGrant(internalId: String, role: Role): Role {
        if (role != Role.GUEST) {
            return role
        }
        return if (trialGrantService.existsByInternalId(internalId)) Role.TRIAL else Role.GUEST
    }

    fun applyAdminGrant(internalId: String, role: Role): Role {
        if (role == Role.SUPERUSER) {
            return role
        }
        return if (adminGrantService.existsByInternalId(internalId)) Role.ADMIN else role
    }

    private fun authoritiesFor(role: Role): List<GrantedAuthority> =
        when (role) {
            Role.SUPERUSER -> listOf(
                SimpleGrantedAuthority("ROLE_SUPERUSER"),
                SimpleGrantedAuthority("ROLE_ADMIN"),
            )
            else -> listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
        }

    fun cookingClubIdsLedBy(foodexUser: FoodExOidcUser, knownClubIds: Set<Int>): Set<Int> =
        foodexUser.executiveAtCircles
            .map { it.id.toInt() }
            .toSet()
            .intersect(knownClubIds)

    // Refresh cooking-club leadership join table
    fun reloadPermissionsOfUserToCookingClubs(user: UserEntity, leaderAtClubIds: Set<Int>) {
        val currentlyLeading = user.leaderAt.toList()

        for (club in currentlyLeading) {
            cookingClubService.removeLeaderFromCookingClub(user.id, club.id)
        }

        if (user.role.isAdminOrAbove()) {
            for (club in cookingClubService.getAllCookingClubs()) {
                cookingClubService.addLeaderToCookingClub(user.id, club.id)
            }
            return
        }

        for (clubId in leaderAtClubIds) {
            runCatching {
                cookingClubService.addLeaderToCookingClub(user.id, clubId)
            }
        }
    }
}
