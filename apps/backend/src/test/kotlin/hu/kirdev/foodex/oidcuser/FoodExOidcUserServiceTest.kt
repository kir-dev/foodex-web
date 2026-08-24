package hu.kirdev.foodex.oidcuser

import hu.kirdev.foodex.admingrant.AdminGrantService
import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.cookingclub.DetailedCookingClubDto
import hu.kirdev.foodex.trialgrant.TrialGrantService
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
    private lateinit var trialGrantService: TrialGrantService
    private lateinit var adminGrantService: AdminGrantService
    private lateinit var service: FoodExOidcUserService

    private val superuserId = "superuser-uuid"

    @BeforeEach
    fun setUp() {
        userService = mockk(relaxed = true)
        cookingClubService = mockk(relaxed = true)
        trialGrantService = mockk(relaxed = true)
        adminGrantService = mockk(relaxed = true)
        service = FoodExOidcUserService(
            userService,
            cookingClubService,
            setOf(superuserId),
            trialGrantService,
            adminGrantService,
        )
    }

    @Test
    fun `getHighestRole elevates hardcoded superuser`() {
        val user = foodExUser(subject = superuserId)
        assertEquals(Role.SUPERUSER, service.getHighestRole(user))
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
    fun `applyTrialGrant upgrades guest when grant exists`() {
        every { trialGrantService.existsByInternalId("guest-1") } returns true
        assertEquals(Role.TRIAL, service.applyTrialGrant("guest-1", Role.GUEST))
    }

    @Test
    fun `applyTrialGrant leaves guest when no grant`() {
        every { trialGrantService.existsByInternalId("guest-1") } returns false
        assertEquals(Role.GUEST, service.applyTrialGrant("guest-1", Role.GUEST))
    }

    @Test
    fun `applyTrialGrant does not override newbie member or admin`() {
        every { trialGrantService.existsByInternalId(any()) } returns true
        assertEquals(Role.MEMBER, service.applyTrialGrant("member-1", Role.MEMBER))
        assertEquals(Role.ADMIN, service.applyTrialGrant("admin-1", Role.ADMIN))
        assertEquals(Role.NEWBIE, service.applyTrialGrant("newbie-1", Role.NEWBIE))
        assertEquals(Role.SUPERUSER, service.applyTrialGrant("su-1", Role.SUPERUSER))
    }

    @Test
    fun `applyAdminGrant upgrades non-superuser when grant exists`() {
        every { adminGrantService.existsByInternalId("member-1") } returns true
        assertEquals(Role.ADMIN, service.applyAdminGrant("member-1", Role.MEMBER))
        assertEquals(Role.ADMIN, service.applyAdminGrant("member-1", Role.GUEST))
        assertEquals(Role.ADMIN, service.applyAdminGrant("member-1", Role.TRIAL))
        assertEquals(Role.ADMIN, service.applyAdminGrant("member-1", Role.NEWBIE))
    }

    @Test
    fun `applyAdminGrant never overrides superuser`() {
        every { adminGrantService.existsByInternalId(any()) } returns true
        assertEquals(Role.SUPERUSER, service.applyAdminGrant("su-1", Role.SUPERUSER))
    }

    @Test
    fun `resolveRole trial grant only applies after guest`() {
        every { trialGrantService.existsByInternalId("guest-1") } returns true
        every { adminGrantService.existsByInternalId("guest-1") } returns false
        val guest = foodExUser(subject = "guest-1")
        assertEquals(Role.TRIAL, service.resolveRole(guest))
    }

    @Test
    fun `resolveRole newbie wins over trial grant`() {
        every { trialGrantService.existsByInternalId("newbie-1") } returns true
        every { adminGrantService.existsByInternalId("newbie-1") } returns false
        val newbie = foodExUser(
            subject = "newbie-1",
            memberships = listOf(
                mapOf("id" to 182L, "name" to "FoodEx", "title" to listOf("újonc")),
            ),
        )
        assertEquals(Role.NEWBIE, service.resolveRole(newbie))
    }

    @Test
    fun `resolveRole admin grant wins over trial`() {
        every { trialGrantService.existsByInternalId("guest-1") } returns true
        every { adminGrantService.existsByInternalId("guest-1") } returns true
        val guest = foodExUser(subject = "guest-1")
        assertEquals(Role.ADMIN, service.resolveRole(guest))
    }

    @Test
    fun `resolveRole hardcoded superuser wins over admin grant`() {
        every { adminGrantService.existsByInternalId(superuserId) } returns true
        val user = foodExUser(subject = superuserId)
        assertEquals(Role.SUPERUSER, service.resolveRole(user))
    }

    @Test
    fun `cookingClubIdsLedBy uses executiveAt of known clubs`() {
        val user = foodExUser(
            subject = "leader-1",
            executiveAt = listOf(mapOf("id" to 403L, "name" to "Americano")),
        )
        assertEquals(setOf(403), service.cookingClubIdsLedBy(user, setOf(403, 473)))
    }

    @Test
    fun `cookingClubIdsLedBy ignores membership without executiveAt`() {
        val user = foodExUser(
            subject = "member-2",
            memberships = listOf(
                mapOf("id" to 403L, "name" to "Americano", "title" to emptyList<String>()),
            ),
        )
        assertEquals(emptySet<Int>(), service.cookingClubIdsLedBy(user, setOf(403, 473)))
    }

    @Test
    fun `cookingClubIdsLedBy ignores FoodEx executive id unless it is a cooking club`() {
        val user = foodExUser(
            subject = "exec-2",
            executiveAt = listOf(
                mapOf("id" to 182L, "name" to "FoodEx"),
                mapOf("id" to 403L, "name" to "Americano"),
            ),
        )
        assertEquals(setOf(403), service.cookingClubIdsLedBy(user, setOf(403, 473)))
    }

    @Test
    fun `cookingClubIdsLedBy ignores executive of unknown club id`() {
        val user = foodExUser(
            subject = "exec-3",
            executiveAt = listOf(mapOf("id" to 999L, "name" to "Other")),
        )
        assertEquals(emptySet<Int>(), service.cookingClubIdsLedBy(user, setOf(403, 473)))
    }

    @Test
    fun `cookingClubIdsLedBy allows guest korvezeto of a known club`() {
        val user = foodExUser(
            subject = "guest-leader",
            executiveAt = listOf(mapOf("id" to 403L, "name" to "Americano")),
        )
        assertEquals(setOf(403), service.cookingClubIdsLedBy(user, setOf(403, 473)))
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

    @Test
    fun `reloadPermissions grants all clubs to superuser`() {
        val user = UserEntity(
            id = 1,
            internalId = "su",
            role = Role.SUPERUSER,
            name = "Superuser",
            nickname = null,
            email = "s@test.com",
            favouriteQuote = null,
            isActive = true,
        )
        every { cookingClubService.getAllCookingClubs() } returns listOf(
            DetailedCookingClubDto(403, "A", emptyList(), emptyList(), emptyList()),
        )
        every { cookingClubService.addLeaderToCookingClub(any(), any()) } returns mockk(relaxed = true)

        service.reloadPermissionsOfUserToCookingClubs(user, emptySet())

        verify { cookingClubService.addLeaderToCookingClub(1, 403) }
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
