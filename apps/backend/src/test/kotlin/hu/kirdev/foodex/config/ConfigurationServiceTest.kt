package hu.kirdev.foodex.config

import hu.kirdev.foodex.openingrequest.OpeningRequestService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ConfigurationServiceTest {

    private lateinit var configurationRepository: ConfigurationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var openingRequestService: OpeningRequestService
    private lateinit var service: ConfigurationService

    private val config = ConfigurationEntity(
        id = 1,
        feelingOfTheWeek = "teszt feeling",
        foodExLogo = "logo.png",
        homepageDescription = "leírás",
        startOfSemester = LocalDateTime.of(2026, 8, 1, 0, 0),
        endOfSemester = LocalDateTime.of(2027, 2, 1, 0, 0),
    )

    @BeforeEach
    fun setUp() {
        configurationRepository = mockk()
        userRepository = mockk()
        openingRequestService = mockk()
        service = ConfigurationService(configurationRepository, userRepository, openingRequestService)

        every { configurationRepository.findTopByOrderByIdDesc() } returns config
        every { openingRequestService.getCurrentOrUpcomingAcceptedOpeningRequests() } returns emptyList()
    }

    @Test
    fun `getHomepage requests only active ADMIN MEMBER NEWBIE users`() {
        every {
            userRepository.findUserEntitiesByIsActiveTrueAndRoleIn(ConfigurationService.HOMEPAGE_ACTIVE_MEMBER_ROLES)
        } returns emptyList()

        service.getHomepage()

        verify(exactly = 1) {
            userRepository.findUserEntitiesByIsActiveTrueAndRoleIn(ConfigurationService.HOMEPAGE_ACTIVE_MEMBER_ROLES)
        }
        assertEquals(listOf(Role.ADMIN, Role.MEMBER, Role.NEWBIE), ConfigurationService.HOMEPAGE_ACTIVE_MEMBER_ROLES)
    }

    @Test
    fun `getHomepage orders active members by role then case-insensitive name`() {
        val unsorted = listOf(
            user(1, Role.NEWBIE, "Zoli"),
            user(2, Role.MEMBER, "charlie"),
            user(3, Role.ADMIN, "Zara"),
            user(4, Role.NEWBIE, "anna"),
            user(5, Role.MEMBER, "Bob"),
            user(6, Role.ADMIN, "Alma"),
        )
        every {
            userRepository.findUserEntitiesByIsActiveTrueAndRoleIn(ConfigurationService.HOMEPAGE_ACTIVE_MEMBER_ROLES)
        } returns unsorted

        val homepage = service.getHomepage()

        assertEquals(
            listOf(
                6 to Role.ADMIN,
                3 to Role.ADMIN,
                5 to Role.MEMBER,
                2 to Role.MEMBER,
                4 to Role.NEWBIE,
                1 to Role.NEWBIE,
            ),
            homepage.activeMembers.map { it.id to it.role },
        )
        assertEquals("teszt feeling", homepage.feelingOfTheWeek)
        assertEquals("logo.png", homepage.foodExLogo)
        assertEquals("leírás", homepage.homepageDescription)
        assertEquals(emptyList<Any>(), homepage.upcomingOpenings)
    }

    private fun user(id: Int, role: Role, name: String) = UserEntity(
        id = id,
        internalId = "internal-$id",
        role = role,
        name = name,
        nickname = null,
        email = "u$id@test.com",
        favouriteQuote = null,
        isActive = true,
    )
}
