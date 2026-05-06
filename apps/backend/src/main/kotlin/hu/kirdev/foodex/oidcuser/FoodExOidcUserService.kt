package hu.kirdev.foodex.oidcuser

import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.user.UserService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.transaction.annotation.Transactional

open class FoodExOidcUserService(
    val userService: UserService,
    val cookingClubService: CookingClubService
) : OidcUserService() {

    private final val foodExID = 182L

    private final val allCookingClubIds = setOf<Int>(
        223,    // Pizzásch
        403,    // Americano
        179,    // Vödör
        473,    // LángoSCH
        31,     // Kakas
        528,    // Paschta;
        395,    // Palacsintázó
        490,    // ReggeliSCH
        529     // Dobozosch
        // TODO: Magyarosch
    )

    @Transactional(readOnly = false)
    override fun loadUser(userRequest: OidcUserRequest?): OidcUser? {
        val authschUser = super.loadUser(userRequest) ?: return null

        val foodexUser = FoodExOidcUser(authschUser)
        val leaderAt : Set<Int> = foodexUser.memberships.map { it.id.toInt() }.toSet().intersect(allCookingClubIds)

        val user = userService.getUserByInternalId(foodexUser.internalId)

        // Already registered in database
        if (user != null) {

            user.role = getHighestRole(foodexUser)
            user.email = foodexUser.email

//            FYI
//            if (admins.contains(user.uid))
//                user.sysadmin = true

            // TODO
            // checkIfMemberInFoodEx()
            // checkIfExecutiveAtFoodEx()
            // checkIfExecutiveAtCookingClub()


            foodexUser.extraAuthorities = getAuthoritiesFromEntity(foodexUser)  // TODO Is it necessary???

            // Reload permission to Cooking Clubs
            reloadPermissionsOfUserToCookingClubs(user, leaderAt)

            userService.updateUser(user)
        }

        // Not registered in the database
        else {
            val user = UserEntity(
                internalId = foodexUser.internalId,
                role = getHighestRole(foodexUser),
                name = foodexUser.name,
                nickname = foodexUser.nickName,
                email = foodexUser.email,
                favouriteQuote = null,
                isActive = foodexUser.memberships.map { it.id }.contains(foodExID),
                profilePicture = foodexUser.profile,
                // TODO: profile etc.
            )
            foodexUser.extraAuthorities = getAuthoritiesFromEntity(foodexUser) // TODO Is it necessary???

            // Reload permission to Cooking Clubs
            reloadPermissionsOfUserToCookingClubs(user, leaderAt)

            userService.updateUser(user)
        }

        return foodexUser
    }

    // Is it necessary???
    private fun getAuthoritiesFromEntity(foodexUser: FoodExOidcUser): List<GrantedAuthority> {
        val authorities: MutableList<GrantedAuthority> = ArrayList()
        authorities.add(SimpleGrantedAuthority("ROLE_${Role.GUEST.name}"))

        // TODO Member vs Newbie vs Executives ???????????????????????????????????????????????

        if (foodexUser.memberships.any { it.id == foodExID }) {
            authorities.add(SimpleGrantedAuthority("ROLE_${Role.MEMBER.name}"))
        }

        return authorities
    }

    private fun getHighestRole(foodexUser: FoodExOidcUser): Role {

        // Admin of FoodEx
        if (foodexUser.executiveAtCircles.any { it.id == foodExID }) {
            return Role.ADMIN
        }

        // Member of FoodEx
        for(membership in foodexUser.memberships) {
            if (membership.id == foodExID) {
                if (membership.title.contains("újonc")) {
                    return Role.NEWBIE
                }
                return Role.MEMBER
            }
        }

        // Guest or Ex-member
        return Role.GUEST
    }

    private fun reloadPermissionsOfUserToCookingClubs(user: UserEntity, leaderAt: Set<Int>) {
        // Remove all permissions of user
        for (club in user.leaderAt) {
            cookingClubService.removeLeaderFromCookingClub(user.id, club.id)
        }

        // Admin --> Add privileges to ALL clubs
        if (user.role == Role.ADMIN) {
            for (club in cookingClubService.getAllCookingClubs()) {
                cookingClubService.addLeaderToCookingClub(user.id, club.id)
            }
            return
        }

        // Add privileges to clubs
        for (club in user.leaderAt) {
            cookingClubService.addLeaderToCookingClub(user.id, club.id)
        }
    }
}