package hu.kirdev.foodex.shift

import hu.kirdev.foodex.config.ConfigurationService
import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.openingrequest.OpeningRequestService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.LocalDateTime

@Service
class ShiftService(
    private val shiftRepository: ShiftRepository,
    private val userRepository: UserRepository,
    private val cookingClubRepository: CookingClubRepository,
    private val openingRequestRepository: OpeningRequestRepository,
    private val openingRequestService: OpeningRequestService,
    private val configurationService: ConfigurationService,
) {

    @Transactional(readOnly = true)
    fun getAllShifts() : List<DetailedShiftDto> {
        return shiftRepository.findAll().map { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = true)
    fun getAllShiftsInSemester(): List<DetailedShiftDto> {
        val config = configurationService.get()
        return shiftRepository.findAll().filter {
            config.startOfSemester < it.closing && it.opening < config.endOfSemester
        }.map { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUpcomingShifts(): List<DetailedShiftDto> {
        val now = LocalDateTime.now()
        return shiftRepository.findAll().filter { it.closing > now }.map { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = true)
    fun getShiftById(id: Int): DetailedShiftDto {
        return shiftRepository.findById(id)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found") }
            .let { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUpcomingActiveAndFullShifts(): ActiveAndFullShifts {
        return ActiveAndFullShifts(
            activeShifts = getActiveShifts(),
            fullShifts = getFullShifts(),
        )
    }

    @Transactional(readOnly = true)
    fun getActiveShifts(): List<DetailedShiftDto> {
        return getUpcomingShifts().filter {
            it.members.size < it.maxMembers
            || it.newbies.size < it.members.size // TODO: ???
        }
    }

    @Transactional(readOnly = true)
    fun getFullShifts(): List<DetailedShiftDto> {
        return getUpcomingShifts().filter {
            it.members.size >= it.maxMembers
            && it.newbies.size >= it.members.size   // TODO: ???
        }
    }

    @Transactional(readOnly = false)
    fun createShift(shift: CreateShiftDto): DetailedShiftDto {
        val club = cookingClubRepository.findById(shift.cookingClubId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found") }

        return shiftRepository.save(ShiftEntity(
            cookingClub = club,
            maxMembers = shift.maxMembers,
            opening = shift.opening,
            closing = shift.closing,
            place = shift.place,
            comment = shift.comment,
        )).let { DetailedShiftDto(it) }
    }

    @Transactional(readOnly = false)
    fun addWorkerToShift(userId: Int, shiftId: Int) : DetailedShiftDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found") }

        if (shift.workers.contains(user)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already added")
        }

        val shiftDto = DetailedShiftDto(shift)
        if ((user.role == Role.MEMBER || user.role != Role.ADMIN)
            && shiftDto.members.size >= shift.maxMembers) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Shift has too many members")
        }
        else if (user.role == Role.NEWBIE && shiftDto.newbies.size >= shiftDto.members.size) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Shift has too many newbies")
        }

        shift.workers.add(user)

        return DetailedShiftDto(shiftRepository.save(shift))
    }

    @Transactional(readOnly = false)
    fun removeWorkerFromShift(userId: Int, shiftId: Int) : DetailedShiftDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found") }

        if(!shift.workers.contains(user)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Worker is not part of shift")
        }

        shift.workers.remove(user)

        return DetailedShiftDto(shiftRepository.save(shift))
    }

    @Transactional(readOnly = false)
    fun deleteShift(shiftId: Int) {
        shiftRepository.deleteById(shiftId)
    }

    @Transactional(readOnly = false)
    fun updateShift(shiftId: Int, toUpdate: UpdateShiftDto): DetailedShiftDto {
        val shift = shiftRepository.findById(shiftId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found") }

        val club = toUpdate.cookingClubId?.let{ cookingClubRepository.findByIdOrNull(it) }
        club?.let { shift.cookingClub = it }

        toUpdate.maxMembers?.let { shift.maxMembers = it }
        toUpdate.opening?.let { shift.opening = it }
        toUpdate.closing?.let { shift.closing = it }
        toUpdate.place?.let { shift.place = it }
        toUpdate.comment?.let { shift.comment = it }

        return DetailedShiftDto(shiftRepository.save(shift))
    }

    @Transactional(readOnly = false)
    fun createShiftsFromOpeningRequest(openingRequestId: Int, createRequest: CreateShiftFromOpeningRequestDto) : List<DetailedShiftDto> {
        val request = openingRequestRepository.findById(openingRequestId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        val shifts = mutableListOf<ShiftEntity>()

        val lengthOfEachShift: Duration = Duration.between(request.opening, request.closing)
            .dividedBy(createRequest.numberOfShifts.toLong())

        for (i in 0..<createRequest.numberOfShifts) {
            val shift = ShiftEntity(
                cookingClub = request.cookingClub,
                maxMembers = createRequest.maxMembers,
                opening = request.opening.plus(lengthOfEachShift.multipliedBy(i.toLong())),
                closing = request.opening.plus(lengthOfEachShift.multipliedBy((i + 1).toLong())),
                place = request.place
            )
            shiftRepository.save(shift)
            shifts.add(shift)
        }

        openingRequestService.acceptOpeningRequest(openingRequestId)

        return shifts.map { DetailedShiftDto(it) }
    }
}