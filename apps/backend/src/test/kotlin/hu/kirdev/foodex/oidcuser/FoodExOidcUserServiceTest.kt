package hu.kirdev.foodex.oidcuser

import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.cookingclub.DetailedCookingClubDto
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import java.time.Instant

class FoodExOidcUserServiceTest {

    private lateinit var userService: UserService
    private lateinit var cookingClubService: CookingClubService
    private lateinit var service: FoodExOidcUserService

    private val developerId = "dev-admin-uuid"

    @BeforeEach
    fun setUp() {
        userService = mockk(relaxed = true)
        cookingClubService = mockk(relaxed = true)
        service = FoodExOidcUserService(
            userService,
            cookingClubService,
            setOf(developerId),
        )
    }

    @Test
    fun `getHighestRole elevates developer admin`() {
        val user = foodExUser(subject = developerId)
        assertEquals(Role.ADMIN, service.getHighestRole(user))
    }

    @Test
    fun `getHighestRole guest when no membership`() {
        val user = foodExUser(subject = "random-user")
        assertEquals(Role.GUEST, service.getHighestRole(user))
    }

    @Test
    fun `getHighestRole member of FoodEx`() {
        val user = foodExUser(
            subject = "member-1",
            memberships = listOf(mapOf("id" to 182L, "name" to "FoodEx", "title" to emptyList<String>())),
        )
        assertEquals(Role.MEMBER, service.getHighestRole(user))
    }

    @Test
    fun `getHighestRole newbie when title contains ujonc`() {
        val user = foodExUser(
            subject = "newbie-1",
            memberships = listOf(
                mapOf("id" to 182L, "name" to "FoodEx", "title" to listOf("újonc")),
            ),
        )
        assertEquals(Role.NEWBIE, service.getHighestRole(user))
    }

    @Test
    fun `getHighestRole admin when executive at FoodEx`() {
        val user = foodExUser(
            subject = "exec-1",
            executiveAt = listOf(mapOf("id" to 182L, "name" to "FoodEx")),
        )
        assertEquals(Role.ADMIN, service.getHighestRole(user))
    }

    @Test
    fun `reloadPermissions uses AuthSCH club ids for non-admin`() {
        val user = UserEntity(
            id = 7,
            internalId = "x",
            role = Role.MEMBER,
            name = "Leader",
            nickname = null,
            email = "l@test.com",
            favouriteQuote = null,
            isActive = true,
            leaderAt = mutableListOf(),
        )
        every { cookingClubService.removeLeaderFromCookingClub(any(), any()) } returns mockk(relaxed = true)
        every { cookingClubService.addLeaderToCookingClub(any(), any()) } returns mockk(relaxed = true)

        service.reloadPermissionsOfUserToCookingClubs(user, setOf(403, 473))

        verify(exactly = 1) { cookingClubService.addLeaderToCookingClub(7, 403) }
        verify(exactly = 1) { cookingClubService.addLeaderToCookingClub(7, 473) }
        verify(exactly = 0) { cookingClubService.getAllCookingClubs() }
    }

    @Test
    fun `reloadPermissions grants all clubs to admin`() {
        val user = UserEntity(
            id = 1,
            internalId = "admin",
            role = Role.ADMIN,
            name = "Admin",
            nickname = null,
            email = "a@test.com",
            favouriteQuote = null,
            isActive = true,
        )
        every { cookingClubService.getAllCookingClubs() } returns listOf(
            DetailedCookingClubDto(403, "A", emptyList(), emptyList(), emptyList()),
            DetailedCookingClubDto(473, "B", emptyList(), emptyList(), emptyList()),
        )
        every { cookingClubService.addLeaderToCookingClub(any(), any()) } returns mockk(relaxed = true)

        service.reloadPermissionsOfUserToCookingClubs(user, emptySet())

        verify { cookingClubService.addLeaderToCookingClub(1, 403) }
        verify { cookingClubService.addLeaderToCookingClub(1, 473) }
    }

    private fun foodExUser(
        subject: String,
        memberships: List<Map<String, Any>> = emptyList(),
        executiveAt: List<Map<String, Any>> = emptyList(),
    ): FoodExOidcUser {
        val claims = mutableMapOf<String, Any>(
            "sub" to subject,
            "name" to "Test User",
            "email" to "test@example.com",
        )
        if (memberships.isNotEmpty()) {
            claims["pek.sch.bme.hu:activeMemberships/v1"] = memberships
        }
        if (executiveAt.isNotEmpty()) {
            claims["pek.sch.bme.hu:executiveAt/v1"] = executiveAt
        }
        val idToken = OidcIdToken(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            claims,
        )
        val oidcUser: OidcUser = DefaultOidcUser(emptyList(), idToken)
        return FoodExOidcUser(oidcUser)
    }
}
