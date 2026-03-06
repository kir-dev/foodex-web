package hu.kirdev.foodex.service

import hu.kirdev.foodex.model.ExecutiveAt
import hu.kirdev.foodex.model.FoodExOidcUser
import hu.kirdev.foodex.model.Role
import hu.kirdev.foodex.model.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.transaction.annotation.Transactional
import kotlin.text.toInt

open class FoodExOidcUserService(val userService: UserService, val cookingClubService: CookingClubService) :
    OidcUserService() {
    private final val foodExID = 182L

    private final val allCookingClubIds = setOf<Int>(
        223,    // Pizzásch
        403,    // Americano
        179,    // Vödör
        473,    // LángoSCH
        31,     // Kakas
        528,    // Paschta;
        395,    // Palacsintázó
        490     // ReggeliSCH
        // TODO: Dobozosch & Magyarosch
    )

    @Transactional(readOnly = false)
    override fun loadUser(userRequest: OidcUserRequest?): OidcUser? {
        val authschUser = super.loadUser(userRequest) ?: return null

        val foodexUser = FoodExOidcUser(authschUser)

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
            reloadPermissionsOfUserToCookingClubs(user.id, foodexUser.executiveAtCircles.map { it.id.toInt() }.toSet())

            userService.updateUser(user)
        }

        // Not registered in the database
        else {
            val user = UserEntity(
                internalId = foodexUser.internalId,
                role = getHighestRole(foodexUser),
                name = foodexUser.name,
                nickname = foodexUser.nickName?:"",
                email = foodexUser.email,
                isActive = true
                // TODO: profile etc.
            )
            foodexUser.extraAuthorities = getAuthoritiesFromEntity(foodexUser) // TODO Is it necessary???

            // Reload permission to Cooking Clubs
            reloadPermissionsOfUserToCookingClubs(user.id, foodexUser.executiveAtCircles.map { it.id.toInt() }.toSet())

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

    private fun reloadPermissionsOfUserToCookingClubs(userId: Int, executiveAtCircles: Set<Int>) {
        removeAllCookingClubPermissionsOfUser(userId)
        addCookingClubAdminPermissionsToUser(userId, executiveAtCircles intersect allCookingClubIds)
    }

    private fun removeAllCookingClubPermissionsOfUser(userId: Int) {
        val clubs = cookingClubService.getAllCookingClubsOfUser(userId)

        for (club in clubs) {
            cookingClubService.removeLeaderFromCookingClub(club.id, userId)
        }
    }

    private fun addCookingClubAdminPermissionsToUser(userId: Int, cookingClubs: Set<Int>) {
        for (clubId in cookingClubs) {
            cookingClubService.addLeaderToCookingClub(clubId, userId)
        }
    }
}