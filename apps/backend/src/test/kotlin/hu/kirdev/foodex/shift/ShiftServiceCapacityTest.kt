package hu.kirdev.foodex.shift

import hu.kirdev.foodex.config.ConfigurationService
import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.openingrequest.OpeningRequestService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.Optional

class ShiftServiceCapacityTest {

    private lateinit var shiftRepository: ShiftRepository
    private lateinit var userRepository: UserRepository
    private lateinit var cookingClubRepository: CookingClubRepository
    private lateinit var cookingClubService: CookingClubService
    private lateinit var openingRequestRepository: OpeningRequestRepository
    private lateinit var openingRequestService: OpeningRequestService
    private lateinit var configurationService: ConfigurationService
    private lateinit var service: ShiftService

    private val club = CookingClubEntity(id = 403, name = "Americano")
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        shiftRepository = mockk()
        userRepository = mockk()
        cookingClubRepository = mockk()
        cookingClubService = mockk()
        openingRequestRepository = mockk()
        openingRequestService = mockk()
        configurationService = mockk()
        service = ShiftService(
            shiftRepository,
            userRepository,
            cookingClubRepository,
            cookingClubService,
            openingRequestRepository,
            openingRequestService,
            configurationService,
        )
    }

    @Test
    fun `canJoin member when under maxMembers`() {
        val shift = shift(maxMembers = 2, workers = mutableListOf(user(1, Role.MEMBER)))
        assertTrue(service.canJoin(user(2, Role.MEMBER), shift))
    }

    @Test
    fun `canJoin member false when full`() {
        val shift = shift(
            maxMembers = 2,
            workers = mutableListOf(user(1, Role.MEMBER), user(2, Role.ADMIN)),
        )
        assertFalse(service.canJoin(user(3, Role.MEMBER), shift))
    }

    @Test
    fun `canJoin newbie requires a member and free newbie slot`() {
        val empty = shift(maxMembers = 5, workers = mutableListOf())
        assertFalse(service.canJoin(user(9, Role.NEWBIE), empty))

        val withMember = shift(maxMembers = 5, workers = mutableListOf(user(1, Role.MEMBER)))
        assertTrue(service.canJoin(user(9, Role.NEWBIE), withMember))

        val newbieFull = shift(
            maxMembers = 5,
            workers = mutableListOf(user(1, Role.MEMBER), user(8, Role.NEWBIE)),
        )
        assertFalse(service.canJoin(user(9, Role.NEWBIE), newbieFull))
    }

    @Test
    fun `canJoin guest always false`() {
        val shift = shift(maxMembers = 5, workers = mutableListOf())
        assertFalse(service.canJoin(user(1, Role.GUEST), shift))
    }

    @Test
    fun `hasOpenSlot partitions active vs full`() {
        val active = shift(maxMembers = 2, workers = mutableListOf(user(1, Role.MEMBER)))
        assertTrue(service.hasOpenSlot(active))

        val full = shift(
            maxMembers = 1,
            workers = mutableListOf(user(1, Role.MEMBER), user(2, Role.NEWBIE)),
        )
        // member full and newbie slot full (1 newbie for 1 member)
        assertFalse(service.hasOpenSlot(full))
    }

    @Test
    fun `addWorkerToShift rejects guest with 403`() {
        val actor = user(1, Role.ADMIN)
        val guest = user(2, Role.GUEST)
        val shift = shift(maxMembers = 5, workers = mutableListOf())
        every { userRepository.findById(2) } returns Optional.of(guest)
        every { shiftRepository.findById(1) } returns Optional.of(shift)

        val ex = assertThrows<ResponseStatusException> {
            service.addWorkerToShift(2, 1, actor)
        }
        assertTrue(ex.statusCode == HttpStatus.FORBIDDEN)
    }

    @Test
    fun `addWorkerToShift admin can add member when capacity is full`() {
        val admin = user(1, Role.ADMIN)
        val newMember = user(3, Role.MEMBER)
        val shift = shift(
            maxMembers = 1,
            workers = mutableListOf(user(2, Role.MEMBER)),
        )
        every { userRepository.findById(3) } returns Optional.of(newMember)
        every { shiftRepository.findById(1) } returns Optional.of(shift)
        every { shiftRepository.save(shift) } returns shift

        val result = service.addWorkerToShift(3, 1, admin)

        assertTrue(result.members.any { it.id == 3 })
    }

    @Test
    fun `addWorkerToShift rejects duplicate with 409`() {
        val member = user(2, Role.MEMBER)
        val actor = user(2, Role.MEMBER)
        val shift = shift(maxMembers = 5, workers = mutableListOf(member))
        every { userRepository.findById(2) } returns Optional.of(member)
        every { shiftRepository.findById(1) } returns Optional.of(shift)

        val ex = assertThrows<ResponseStatusException> {
            service.addWorkerToShift(2, 1, actor)
        }
        assertTrue(ex.statusCode == HttpStatus.CONFLICT)
    }

    private fun shift(maxMembers: Int, workers: MutableList<UserEntity>) = ShiftEntity(
        id = 1,
        cookingClub = club,
        maxMembers = maxMembers,
        opening = now.minusHours(1),
        closing = now.plusHours(2),
        place = "kitchen",
        comment = "",
        workers = workers,
    )

    private fun user(id: Int, role: Role) = UserEntity(
        id = id,
        internalId = "internal-$id",
        role = role,
        name = "User $id",
        nickname = null,
        email = "u$id@test.com",
        favouriteQuote = null,
        isActive = true,
    )
}
