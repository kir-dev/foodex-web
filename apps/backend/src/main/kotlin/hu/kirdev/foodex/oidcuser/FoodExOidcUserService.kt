package hu.kirdev.foodex.oidcuser

import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.newbiegrant.NewbieGrantService
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
    @param:Qualifier("developerAdminIds") private val developerAdminIds: Set<String>,
    private val newbieGrantService: NewbieGrantService,
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
        val role = applyNewbieGrant(foodexUser.internalId, getHighestRole(foodexUser))

        val user = if (existing != null) {
            existing.role = role
            existing.email = foodexUser.requiredEmail
            existing
        } else {
            UserEntity(
                internalId = foodexUser.internalId,
                role = role,
                name = foodexUser.requiredName,
                nickname = foodexUser.nickName,
                email = foodexUser.requiredEmail,
                favouriteQuote = null,
                isActive = foodexUser.memberships.map { it.id }.contains(foodExID) || role == Role.ADMIN,
                profilePicture = foodexUser.profile,
            )
        }

        // Persist first so leadership ops have a real user id
        val saved = userService.updateUser(user)
        reloadPermissionsOfUserToCookingClubs(saved, leaderAt)
        foodexUser.extraAuthorities = authoritiesFor(saved.role)

        return foodexUser
    }

    fun getHighestRole(foodexUser: FoodExOidcUser): Role {
        // Developer admin elevators (AuthSCH internalId)
        if (foodexUser.internalId in developerAdminIds) {
            return Role.ADMIN
        }

        // Admin of FoodEx
        if (foodexUser.executiveAtCircles.any { it.id == foodExID }) {
            return Role.ADMIN
        }

        // Member of FoodEx
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

    fun applyNewbieGrant(internalId: String, role: Role): Role {
        if (role != Role.GUEST) {
            return role
        }
        return if (newbieGrantService.existsByInternalId(internalId)) Role.NEWBIE else Role.GUEST
    }

    private fun authoritiesFor(role: Role): List<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    fun cookingClubIdsLedBy(foodexUser: FoodExOidcUser, knownClubIds: Set<Int>): Set<Int> =
        foodexUser.executiveAtCircles
            .map { it.id.toInt() }
            .toSet()
            .intersect(knownClubIds)

    // Refresh cooking-club leadership join table
    fun reloadPermissionsOfUserToCookingClubs(user: UserEntity, leaderAtClubIds: Set<Int>) {
        val currentlyLeading = user.leaderAt.toList()


        // Remove all permissions of user
        for (club in currentlyLeading) {
            cookingClubService.removeLeaderFromCookingClub(user.id, club.id)
        }

        // Admin --> Add privileges to ALL clubs
        if (user.role == Role.ADMIN) {
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
