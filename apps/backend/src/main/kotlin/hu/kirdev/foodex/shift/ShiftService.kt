package hu.kirdev.foodex.shift

import hu.kirdev.foodex.config.ConfigurationService
import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.openingrequest.OpeningRequestService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime

@Service
class ShiftService(
    private val shiftRepository: ShiftRepository,
    private val userRepository: UserRepository,
    private val cookingClubRepository: CookingClubRepository,
    private val cookingClubService: CookingClubService,
    private val openingRequestRepository: OpeningRequestRepository,
    private val openingRequestService: OpeningRequestService,
    private val configurationService: ConfigurationService,
    private val clock: Clock,
) {

    @Transactional(readOnly = true)
    fun getAllShifts(): List<DetailedShiftDto> {
        return shiftRepository.findAll().map { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = true)
    fun getAllShiftsInSemester(): List<DetailedShiftDto> {
        val config = configurationService.get()
        return shiftRepository
            .findOverlappingSemester(config.startOfSemester, config.endOfSemester)
            .map { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUpcomingShiftEntities(): List<ShiftEntity> {
        return shiftRepository.findUpcomingWithClub(now())
    }

    @Transactional(readOnly = true)
    fun getUpcomingShifts(): List<DetailedShiftDto> {
        return getUpcomingShiftEntities().map { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = true)
    fun getShiftById(id: Int): DetailedShiftDto {
        val shift = shiftRepository.findByIdWithClub(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found")
        return DetailedShiftDto(shift)
    }

    @Transactional(readOnly = true)
    fun getShiftEntity(id: Int): ShiftEntity {
        return shiftRepository.findByIdWithClub(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found")
    }

    @Transactional(readOnly = true)
    fun getUpcomingActiveAndFullShifts(): ActiveAndFullShifts {
        val now = now()
        val upcoming = getUpcomingShiftEntities()
        val active = mutableListOf<ShiftEntity>()
        val fullOrHappening = mutableListOf<ShiftEntity>()
        val notYetOpen = mutableListOf<ShiftEntity>()
        for (shift in upcoming) {
            val notStarted = shift.opening.isAfter(now)
            when {
                notStarted && !isApplicationOpen(shift, now) -> notYetOpen.add(shift)
                notStarted && hasMemberSlot(shift) -> active.add(shift)
                else -> fullOrHappening.add(shift)
            }
        }
        notYetOpen.sortWith(compareBy({ it.applicationOpening }, { it.opening }, { it.id }))
        return ActiveAndFullShifts(
            activeShifts = active.map { DetailedShiftDto(it) },
            fullShifts = fullOrHappening.map { DetailedShiftDto(it) },
            notYetOpenShifts = notYetOpen.map { DetailedShiftDto(it) },
        )
    }

    @Transactional(readOnly = true)
    fun getShiftsForOpeningRequest(openingRequestId: Int): List<DetailedShiftDto> {
        openingRequestService.getOpeningRequestEntity(openingRequestId)
        return shiftRepository.findAllByOpeningRequestIdWithClub(openingRequestId)
            .map { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = false)
    fun createShift(shift: CreateShiftDto, actor: UserEntity): DetailedShiftDto {
        val request = openingRequestRepository.findById(shift.openingRequestId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }
        val club = request.cookingClub

        requireLeaderOrAdmin(actor, club.id)

        if (shiftRepository.countByOpeningRequestId(request.id) >= MAX_SHIFTS_PER_OPENING_REQUEST) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Opening request already has the maximum of $MAX_SHIFTS_PER_OPENING_REQUEST shifts",
            )
        }

        if (!shift.opening.isBefore(shift.closing)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing must be after opening")
        }
        if (shift.maxMembers < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "maxMembers must be positive")
        }
        if (shift.maxMembers > 6) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "maxMembers must be at most 6")
        }
        requireApplicationOpeningBeforeShiftStart(shift.applicationOpening, shift.opening)

        return shiftRepository.save(
            ShiftEntity(
                cookingClub = club,
                maxMembers = shift.maxMembers,
                opening = shift.opening,
                closing = shift.closing,
                place = shift.place,
                comment = shift.comment,
                applicationOpening = shift.applicationOpening,
                openingRequest = request,
            )
        ).let { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = false)
    fun addWorkerToShift(userId: Int, shiftId: Int, actor: UserEntity): DetailedShiftDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found") }

        requireSelfLeaderOrAdmin(actor, userId, shift.cookingClub.id)

        val now = now()
        val selfJoin = actor.id == user.id
        if (selfJoin && !isApplicationOpen(shift, now)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Applications are not yet open")
        }

        if (!actor.role.isAdminOrAbove() && !shift.opening.isAfter(now)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Shift has already started")
        }

        if (shift.workers.any { it.id == user.id }) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already added")
        }

        when {
            !user.role.hasMemberPrivileges() ->
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Guests cannot join shifts")
            !canJoin(user, shift) && !(actor.role.isAdminOrAbove() && actor.id != user.id) ->
                throw ResponseStatusException(HttpStatus.CONFLICT, "Shift capacity full for this role")
        }

        shift.workers.add(user)
        return DetailedShiftDto(shiftRepository.save(shift))
    }

    @Transactional(readOnly = false)
    fun removeWorkerFromShift(userId: Int, shiftId: Int, actor: UserEntity): DetailedShiftDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found") }

        requireSelfLeaderOrAdmin(actor, userId, shift.cookingClub.id)

        if (!shift.workers.any { it.id == user.id }) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Worker is not part of shift")
        }

        shift.workers.removeIf { it.id == user.id }
        return DetailedShiftDto(shiftRepository.save(shift))
    }

    @Transactional(readOnly = false)
    fun deleteShift(shiftId: Int, actor: UserEntity) {
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found") }

        requireLeaderOrAdmin(actor, shift.cookingClub.id)
        shiftRepository.delete(shift)
    }

    @Transactional(readOnly = false)
    fun updateShift(shiftId: Int, toUpdate: UpdateShiftDto, actor: UserEntity): DetailedShiftDto {
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found") }

        requireLeaderOrAdmin(actor, shift.cookingClub.id)

        val club = toUpdate.cookingClubId?.let { cookingClubRepository.findByIdOrNull(it) }
        if (toUpdate.cookingClubId != null && club == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found")
        }
        club?.let {
            requireLeaderOrAdmin(actor, it.id)
            shift.cookingClub = it
        }

        toUpdate.maxMembers?.let {
            if (it < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "maxMembers must be positive")
            if (it > 6) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "maxMembers must be at most 6")
            shift.maxMembers = it
        }
        toUpdate.opening?.let { shift.opening = it }
        toUpdate.closing?.let { shift.closing = it }
        toUpdate.place?.let { shift.place = it }
        toUpdate.comment?.let { shift.comment = it }
        toUpdate.applicationOpening?.let { shift.applicationOpening = it }

        if (!shift.opening.isBefore(shift.closing)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing must be after opening")
        }
        requireApplicationOpeningBeforeShiftStart(shift.applicationOpening, shift.opening)

        return DetailedShiftDto(shiftRepository.save(shift))
    }

    @Transactional(readOnly = false)
    fun createShiftsFromOpeningRequest(
        openingRequestId: Int,
        createRequest: CreateShiftFromOpeningRequestDto,
        actor: UserEntity,
    ): List<DetailedShiftDto> {
        if (createRequest.numberOfShifts < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "numberOfShifts must be positive")
        }
        if (createRequest.numberOfShifts > MAX_SHIFTS_PER_OPENING_REQUEST) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "numberOfShifts must be at most $MAX_SHIFTS_PER_OPENING_REQUEST",
            )
        }
        if (createRequest.maxMembers < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "maxMembers must be positive")
        }
        if (createRequest.maxMembers > 6) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "maxMembers must be at most 6")
        }

        val request = openingRequestRepository.findById(openingRequestId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        if (!actor.role.isAdminOrAbove()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin required")
        }

        if (request.isAccepted) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Opening request already accepted")
        }

        requireApplicationOpeningBeforeShiftStart(createRequest.applicationOpening, request.opening)

        val existing = shiftRepository.countByOpeningRequestId(openingRequestId)
        if (existing + createRequest.numberOfShifts > MAX_SHIFTS_PER_OPENING_REQUEST) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Opening request already has the maximum of $MAX_SHIFTS_PER_OPENING_REQUEST shifts",
            )
        }

        val lengthOfEachShift: Duration = Duration.between(request.opening, request.closing)
            .dividedBy(createRequest.numberOfShifts.toLong())

        val shifts = mutableListOf<ShiftEntity>()
        for (i in 0 until createRequest.numberOfShifts) {
            val shift = ShiftEntity(
                cookingClub = request.cookingClub,
                maxMembers = createRequest.maxMembers,
                opening = request.opening.plus(lengthOfEachShift.multipliedBy(i.toLong())),
                closing = request.opening.plus(lengthOfEachShift.multipliedBy((i + 1).toLong())),
                place = request.place,
                applicationOpening = createRequest.applicationOpening,
                openingRequest = request,
            )
            shifts.add(shiftRepository.save(shift))
        }

        openingRequestService.acceptOpeningRequest(openingRequestId, actor)

        return shifts.map { DetailedShiftDto(it) }
    }

    // --- capacity helpers (pure / READ on entity state) ---

    fun memberCount(shift: ShiftEntity): Int =
        shift.workers.count { it.role.countsAsMemberForCapacity() }

    fun newbieCount(shift: ShiftEntity): Int =
        shift.workers.count { it.role == Role.NEWBIE }

    fun trialCount(shift: ShiftEntity): Int =
        shift.workers.count { it.role == Role.TRIAL }

    fun canJoin(user: UserEntity, shift: ShiftEntity): Boolean = when (user.role) {
        Role.GUEST, Role.ALUMNI -> false
        Role.NEWBIE, Role.MEMBER, Role.ADMIN, Role.SUPERUSER ->
            memberCount(shift) + newbieCount(shift) < shift.maxMembers
        Role.TRIAL -> trialCount(shift) < memberCount(shift)
    }

    fun hasMemberSlot(shift: ShiftEntity): Boolean =
        memberCount(shift) + newbieCount(shift) < shift.maxMembers

    fun hasOpenSlot(shift: ShiftEntity): Boolean {
        val trialSlot = memberCount(shift) > 0 && trialCount(shift) < memberCount(shift)
        return hasMemberSlot(shift) || trialSlot
    }

    fun isApplicationOpen(shift: ShiftEntity, now: LocalDateTime = LocalDateTime.now(clock)): Boolean {
        val opensAt = shift.applicationOpening ?: return true
        return !opensAt.isAfter(now)
    }

    private fun requireApplicationOpeningBeforeShiftStart(
        applicationOpening: LocalDateTime?,
        opening: LocalDateTime,
    ) {
        if (applicationOpening != null && !applicationOpening.isBefore(opening)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "applicationOpening must be before the opening starts",
            )
        }
    }

    private fun requireLeaderOrAdmin(actor: UserEntity, cookingClubId: Int) {
        if (actor.role.isAdminOrAbove()) return
        if (!cookingClubService.isLeaderOfCookingClub(actor.id, cookingClubId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not leader of cooking club")
        }
    }

    private fun requireSelfLeaderOrAdmin(actor: UserEntity, targetUserId: Int, cookingClubId: Int) {
        if (actor.role.isAdminOrAbove()) return
        if (actor.id == targetUserId) return
        if (!cookingClubService.isLeaderOfCookingClub(actor.id, cookingClubId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this worker")
        }
    }

    private fun now(): LocalDateTime = LocalDateTime.now(clock)

    companion object {
        const val MAX_SHIFTS_PER_OPENING_REQUEST = 4
    }
}
