package hu.kirdev.foodex.shift

import hu.kirdev.foodex.config.ConfigurationService
import hu.kirdev.foodex.config.TimeConfig
import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.openingrequest.OpeningRequestEntity
import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.openingrequest.OpeningRequestService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

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
            Clock.systemDefaultZone(),
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
    fun `canJoin newbie shares maxMembers with members`() {
        val empty = shift(maxMembers = 5, workers = mutableListOf())
        assertTrue(service.canJoin(user(9, Role.NEWBIE), empty))

        val withMember = shift(maxMembers = 5, workers = mutableListOf(user(1, Role.MEMBER)))
        assertTrue(service.canJoin(user(9, Role.NEWBIE), withMember))

        val memberAndNewbieFull = shift(
            maxMembers = 2,
            workers = mutableListOf(user(1, Role.MEMBER), user(8, Role.NEWBIE)),
        )
        assertFalse(service.canJoin(user(9, Role.NEWBIE), memberAndNewbieFull))
        assertFalse(service.canJoin(user(10, Role.MEMBER), memberAndNewbieFull))
    }

    @Test
    fun `canJoin trial requires fewer trials than members excluding newbies`() {
        val empty = shift(maxMembers = 5, workers = mutableListOf())
        assertFalse(service.canJoin(user(9, Role.TRIAL), empty))

        val withNewbieOnly = shift(maxMembers = 5, workers = mutableListOf(user(8, Role.NEWBIE)))
        assertFalse(service.canJoin(user(9, Role.TRIAL), withNewbieOnly))

        val withMember = shift(maxMembers = 5, workers = mutableListOf(user(1, Role.MEMBER)))
        assertTrue(service.canJoin(user(9, Role.TRIAL), withMember))

        val trialFull = shift(
            maxMembers = 5,
            workers = mutableListOf(user(1, Role.MEMBER), user(8, Role.TRIAL)),
        )
        assertFalse(service.canJoin(user(9, Role.TRIAL), trialFull))
    }

    @Test
    fun `canJoin trial allowed when member plus newbie slots are full`() {
        val shift = shift(
            maxMembers = 1,
            workers = mutableListOf(user(1, Role.MEMBER), user(2, Role.NEWBIE)),
        )
        assertTrue(service.canJoin(user(9, Role.TRIAL), shift))
        assertFalse(service.canJoin(user(10, Role.MEMBER), shift))
        assertFalse(service.canJoin(user(11, Role.NEWBIE), shift))
    }

    @Test
    fun `canJoin superuser shares member slots`() {
        val shift = shift(maxMembers = 1, workers = mutableListOf(user(1, Role.MEMBER)))
        assertFalse(service.canJoin(user(2, Role.SUPERUSER), shift))

        val open = shift(maxMembers = 2, workers = mutableListOf(user(1, Role.ADMIN)))
        assertTrue(service.canJoin(user(2, Role.SUPERUSER), open))
    }

    @Test
    fun `canJoin guest always false`() {
        val shift = shift(maxMembers = 5, workers = mutableListOf())
        assertFalse(service.canJoin(user(1, Role.GUEST), shift))
    }

    @Test
    fun `canJoin alumni always false`() {
        val shift = shift(maxMembers = 5, workers = mutableListOf())
        assertFalse(service.canJoin(user(1, Role.ALUMNI), shift))
    }

    @Test
    fun `hasOpenSlot partitions active vs full`() {
        val active = shift(maxMembers = 2, workers = mutableListOf(user(1, Role.MEMBER)))
        assertTrue(service.hasOpenSlot(active))
        assertTrue(service.hasMemberSlot(active))

        val memberNewbieFull = shift(
            maxMembers = 1,
            workers = mutableListOf(user(1, Role.MEMBER), user(2, Role.NEWBIE)),
        )
        assertFalse(service.hasMemberSlot(memberNewbieFull))
        assertTrue(service.hasOpenSlot(memberNewbieFull))

        val trialFull = shift(
            maxMembers = 1,
            workers = mutableListOf(user(1, Role.MEMBER), user(2, Role.TRIAL)),
        )
        assertFalse(service.hasMemberSlot(trialFull))
        assertFalse(service.hasOpenSlot(trialFull))
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
    fun `addWorkerToShift rejects alumni with 403`() {
        val actor = user(1, Role.ADMIN)
        val alumni = user(2, Role.ALUMNI)
        val shift = shift(maxMembers = 5, workers = mutableListOf())
        every { userRepository.findById(2) } returns Optional.of(alumni)
        every { shiftRepository.findById(1) } returns Optional.of(shift)

        val ex = assertThrows<ResponseStatusException> {
            service.addWorkerToShift(2, 1, actor)
        }
        assertTrue(ex.statusCode == HttpStatus.FORBIDDEN)
    }

    @Test
    fun `addWorkerToShift admin self-join still respects capacity`() {
        val admin = user(1, Role.ADMIN)
        val shift = shift(maxMembers = 1, workers = mutableListOf(user(2, Role.MEMBER)))
        every { userRepository.findById(1) } returns Optional.of(admin)
        every { shiftRepository.findById(1) } returns Optional.of(shift)

        val ex = assertThrows<ResponseStatusException> {
            service.addWorkerToShift(1, 1, admin)
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun `addWorkerToShift superuser can add member when capacity is full`() {
        val superuser = user(1, Role.SUPERUSER)
        val newMember = user(3, Role.MEMBER)
        val shift = happeningShift(
            maxMembers = 1,
            workers = mutableListOf(user(2, Role.MEMBER)),
        )
        every { userRepository.findById(3) } returns Optional.of(newMember)
        every { shiftRepository.findById(1) } returns Optional.of(shift)
        every { shiftRepository.save(shift) } returns shift

        val result = service.addWorkerToShift(3, 1, superuser)

        assertTrue(result.members.any { it.id == 3 })
    }

    @Test
    fun `addWorkerToShift admin can add member when capacity is full`() {
        val admin = user(1, Role.ADMIN)
        val newMember = user(3, Role.MEMBER)
        val shift = happeningShift(
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

    @Test
    fun `addWorkerToShift rejects non-admin after shift started`() {
        val member = user(3, Role.MEMBER)
        val actor = user(3, Role.MEMBER)
        val shift = happeningShift(maxMembers = 5, workers = mutableListOf())
        every { userRepository.findById(3) } returns Optional.of(member)
        every { shiftRepository.findById(1) } returns Optional.of(shift)

        val ex = assertThrows<ResponseStatusException> {
            service.addWorkerToShift(3, 1, actor)
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun `createShiftsFromOpeningRequest forbidden for newbie club leader`() {
        val leader = user(8, Role.NEWBIE)
        val request = openingRequest(leader)
        every { openingRequestRepository.findById(10) } returns Optional.of(request)

        val ex = assertThrows<ResponseStatusException> {
            service.createShiftsFromOpeningRequest(
                10,
                CreateShiftFromOpeningRequestDto(
                    maxMembers = 4,
                    numberOfShifts = 2,
                    applicationOpening = now.plusHours(1),
                ),
                leader,
            )
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun `createShiftsFromOpeningRequest rejects more than 4 shifts`() {
        val admin = user(1, Role.ADMIN)
        val request = openingRequest(admin)
        every { openingRequestRepository.findById(10) } returns Optional.of(request)

        val ex = assertThrows<ResponseStatusException> {
            service.createShiftsFromOpeningRequest(
                10,
                CreateShiftFromOpeningRequestDto(
                    maxMembers = 4,
                    numberOfShifts = 5,
                    applicationOpening = now.plusHours(1),
                ),
                admin,
            )
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `createShiftsFromOpeningRequest sets openingRequest on each shift`() {
        val admin = user(1, Role.ADMIN)
        val request = openingRequest(admin)
        every { openingRequestRepository.findById(10) } returns Optional.of(request)
        every { cookingClubService.isLeaderOfCookingClub(1, 403) } returns true
        every { shiftRepository.countByOpeningRequestId(10) } returns 0
        val saved = slot<ShiftEntity>()
        every { shiftRepository.save(capture(saved)) } answers {
            saved.captured.copy(id = 99)
        }
        every { openingRequestService.acceptOpeningRequest(10, admin) } returns mockk()

        val result = service.createShiftsFromOpeningRequest(
            10,
            CreateShiftFromOpeningRequestDto(
                maxMembers = 4,
                numberOfShifts = 2,
                applicationOpening = now.plusHours(1),
            ),
            admin,
        )

        assertEquals(2, result.size)
        assertEquals(10, saved.captured.openingRequest?.id)
    }

    @Test
    fun `getUpcomingActiveAndFullShifts splits by member slot and start time`() {
        val notStartedOpen = shift(
            maxMembers = 2,
            workers = mutableListOf(user(1, Role.MEMBER)),
        )
        val notStartedMemberFull = shift(
            id = 2,
            maxMembers = 1,
            workers = mutableListOf(user(1, Role.MEMBER)),
        )
        val happeningOpen = happeningShift(
            id = 3,
            maxMembers = 4,
            workers = mutableListOf(user(1, Role.MEMBER)),
        )
        val nowSlot = slot<LocalDateTime>()
        every { shiftRepository.findUpcomingWithClub(capture(nowSlot)) } returns listOf(
            notStartedOpen,
            notStartedMemberFull,
            happeningOpen,
        )

        val result = service.getUpcomingActiveAndFullShifts()

        assertEquals(listOf(1), result.activeShifts.map { it.id })
        assertEquals(listOf(2, 3), result.fullShifts.map { it.id })
        assertTrue(result.notYetOpenShifts.isEmpty())
    }

    @Test
    fun `createShiftsFromOpeningRequest copies applicationOpening onto every shift`() {
        val admin = user(1, Role.ADMIN)
        val request = openingRequest(admin)
        val applicationOpening = now.plusHours(1)
        every { openingRequestRepository.findById(10) } returns Optional.of(request)
        every { cookingClubService.isLeaderOfCookingClub(1, 403) } returns true
        every { shiftRepository.countByOpeningRequestId(10) } returns 0
        val saved = mutableListOf<ShiftEntity>()
        every { shiftRepository.save(any()) } answers {
            val entity = firstArg<ShiftEntity>()
            saved.add(entity)
            entity.copy(id = saved.size)
        }
        every { openingRequestService.acceptOpeningRequest(10, admin) } returns mockk()

        val result = service.createShiftsFromOpeningRequest(
            10,
            CreateShiftFromOpeningRequestDto(
                maxMembers = 4,
                numberOfShifts = 2,
                applicationOpening = applicationOpening,
            ),
            admin,
        )

        assertEquals(2, result.size)
        assertTrue(saved.all { it.applicationOpening == applicationOpening })
        assertTrue(result.all { it.applicationOpening == applicationOpening })
    }

    @Test
    fun `createShiftsFromOpeningRequest rejects applicationOpening not before request opening`() {
        val admin = user(1, Role.ADMIN)
        val request = openingRequest(admin)
        every { openingRequestRepository.findById(10) } returns Optional.of(request)

        val ex = assertThrows<ResponseStatusException> {
            service.createShiftsFromOpeningRequest(
                10,
                CreateShiftFromOpeningRequestDto(
                    maxMembers = 4,
                    numberOfShifts = 2,
                    applicationOpening = request.opening,
                ),
                admin,
            )
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `addWorkerToShift rejects member self-join before applicationOpening`() {
        val member = user(3, Role.MEMBER)
        val shift = shift(
            maxMembers = 5,
            workers = mutableListOf(),
            applicationOpening = now.plusHours(1),
        )
        every { userRepository.findById(3) } returns Optional.of(member)
        every { shiftRepository.findById(1) } returns Optional.of(shift)

        val ex = assertThrows<ResponseStatusException> {
            service.addWorkerToShift(3, 1, member)
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
        assertEquals("Applications are not yet open", ex.reason)
    }

    @Test
    fun `addWorkerToShift rejects admin self-join before applicationOpening`() {
        val admin = user(1, Role.ADMIN)
        val shift = shift(
            maxMembers = 5,
            workers = mutableListOf(),
            applicationOpening = now.plusHours(1),
        )
        every { userRepository.findById(1) } returns Optional.of(admin)
        every { shiftRepository.findById(1) } returns Optional.of(shift)

        val ex = assertThrows<ResponseStatusException> {
            service.addWorkerToShift(1, 1, admin)
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
        assertEquals("Applications are not yet open", ex.reason)
    }

    @Test
    fun `addWorkerToShift allows self-join after applicationOpening`() {
        val member = user(3, Role.MEMBER)
        val shift = shift(
            maxMembers = 5,
            workers = mutableListOf(),
            applicationOpening = now.minusMinutes(1),
        )
        every { userRepository.findById(3) } returns Optional.of(member)
        every { shiftRepository.findById(1) } returns Optional.of(shift)
        every { shiftRepository.save(shift) } returns shift

        val result = service.addWorkerToShift(3, 1, member)

        assertTrue(result.members.any { it.id == 3 })
    }

    @Test
    fun `addWorkerToShift admin can assign another user before applicationOpening`() {
        val admin = user(1, Role.ADMIN)
        val member = user(3, Role.MEMBER)
        val shift = shift(
            maxMembers = 5,
            workers = mutableListOf(),
            applicationOpening = now.plusHours(1),
        )
        every { userRepository.findById(3) } returns Optional.of(member)
        every { shiftRepository.findById(1) } returns Optional.of(shift)
        every { shiftRepository.save(shift) } returns shift

        val result = service.addWorkerToShift(3, 1, admin)

        assertTrue(result.members.any { it.id == 3 })
    }

    @Test
    fun `getUpcomingActiveAndFullShifts puts locked shifts in notYetOpenShifts ordered by applicationOpening`() {
        val laterOpen = shift(
            id = 1,
            maxMembers = 4,
            workers = mutableListOf(),
            applicationOpening = now.plusHours(3),
        )
        val earlierOpen = shift(
            id = 2,
            maxMembers = 4,
            workers = mutableListOf(),
            applicationOpening = now.plusHours(1),
        )
        val alreadyOpen = shift(
            id = 3,
            maxMembers = 4,
            workers = mutableListOf(user(1, Role.MEMBER)),
        )
        every { shiftRepository.findUpcomingWithClub(any()) } returns listOf(laterOpen, earlierOpen, alreadyOpen)

        val result = service.getUpcomingActiveAndFullShifts()

        assertEquals(listOf(2, 1), result.notYetOpenShifts.map { it.id })
        assertEquals(listOf(3), result.activeShifts.map { it.id })
        assertTrue(result.fullShifts.isEmpty())
    }

    @Test
    fun `getUpcomingActiveAndFullShifts classifies applicationOpening on Europe Budapest wall clock`() {
        val instant = Instant.parse("2026-09-01T17:22:00Z")
        val opening = LocalDateTime.of(2026, 9, 1, 21, 0)
        val closing = LocalDateTime.of(2026, 9, 1, 23, 0)
        val applicationOpening = LocalDateTime.of(2026, 9, 1, 19, 21)
        val lockedShift = ShiftEntity(
            id = 1,
            cookingClub = club,
            maxMembers = 4,
            opening = opening,
            closing = closing,
            place = "kitchen",
            applicationOpening = applicationOpening,
        )
        every { shiftRepository.findUpcomingWithClub(any()) } returns listOf(lockedShift)

        val budapestResult = serviceWithClock(Clock.fixed(instant, TimeConfig.APP_ZONE))
            .getUpcomingActiveAndFullShifts()
        assertEquals(listOf(1), budapestResult.activeShifts.map { it.id })
        assertTrue(budapestResult.notYetOpenShifts.isEmpty())
        assertTrue(budapestResult.fullShifts.isEmpty())

        val utcResult = serviceWithClock(Clock.fixed(instant, ZoneOffset.UTC))
            .getUpcomingActiveAndFullShifts()
        assertEquals(listOf(1), utcResult.notYetOpenShifts.map { it.id })
        assertTrue(utcResult.activeShifts.isEmpty())
        assertTrue(utcResult.fullShifts.isEmpty())
    }

    @Test
    fun `updateShift persists applicationOpening`() {
        val admin = user(1, Role.ADMIN)
        val shift = shift(maxMembers = 4, workers = mutableListOf())
        val applicationOpening = now.plusMinutes(30)
        every { shiftRepository.findById(1) } returns Optional.of(shift)
        every { shiftRepository.save(shift) } returns shift

        val result = service.updateShift(
            1,
            UpdateShiftDto(
                cookingClubId = null,
                maxMembers = null,
                opening = null,
                closing = null,
                place = null,
                comment = null,
                applicationOpening = applicationOpening,
            ),
            admin,
        )

        assertEquals(applicationOpening, shift.applicationOpening)
        assertEquals(applicationOpening, result.applicationOpening)
    }

    private fun serviceWithClock(clock: Clock) = ShiftService(
        shiftRepository,
        userRepository,
        cookingClubRepository,
        cookingClubService,
        openingRequestRepository,
        openingRequestService,
        configurationService,
        clock,
    )

    private fun shift(
        id: Int = 1,
        maxMembers: Int,
        workers: MutableList<UserEntity>,
        opening: LocalDateTime = now.plusHours(2),
        closing: LocalDateTime = now.plusHours(4),
        applicationOpening: LocalDateTime? = null,
    ) = ShiftEntity(
        id = id,
        cookingClub = club,
        maxMembers = maxMembers,
        opening = opening,
        closing = closing,
        place = "kitchen",
        comment = "",
        applicationOpening = applicationOpening,
        workers = workers,
    )

    private fun happeningShift(
        id: Int = 1,
        maxMembers: Int,
        workers: MutableList<UserEntity>,
    ) = shift(
        id = id,
        maxMembers = maxMembers,
        workers = workers,
        opening = now.minusHours(1),
        closing = now.plusHours(2),
    )

    private fun openingRequest(owner: UserEntity) = OpeningRequestEntity(
        id = 10,
        isAccepted = false,
        user = owner,
        cookingClub = club,
        opening = now.plusHours(2),
        closing = now.plusHours(6),
        place = "kitchen",
        description = "desc",
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
