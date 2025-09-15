package hu.kirdev.foodex.service

import hu.kirdev.foodex.dto.CreateShiftFromRequestDTO
import hu.kirdev.foodex.model.ShiftEntity
import hu.kirdev.foodex.model.UserEntity
import hu.kirdev.foodex.repository.ShiftRepository
import hu.kirdev.foodex.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
class ShiftService(
    private val shiftRepository: ShiftRepository,
    private val userRepository: UserRepository,
    private val configurationService: ConfigurationService,
    private val foodExService: FoodExRequestService
) {

    @Transactional(readOnly = true)
    fun getAllShifts() : List<ShiftEntity> {
        return shiftRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getAllShiftsInSemester(): List<ShiftEntity> {
        return shiftRepository.findAll().filter {
            configurationService.get().startOfSemester < it.closing && it.opening < configurationService.get().startOfSemester
        }
    }

    @Transactional(readOnly = true)
    fun getUpcomingShifts() : List<ShiftEntity> {
        return shiftRepository.findUpcomingShifts()
    }

    @Transactional(readOnly = true)
    fun getShiftById(shiftId: Long) : ShiftEntity? {
        return shiftRepository.findShiftEntityById(shiftId)
    }

    @Transactional(readOnly = true)
    fun getAllShiftsByUserId(userId: Long) : List<ShiftEntity> {
        return shiftRepository.findAllByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getActiveShifts(): List<ShiftEntity> {
        val upcomingShifts = shiftRepository.findUpcomingShifts()
        return upcomingShifts.filter { it.members.size < it.maxMembers }
    }

    @Transactional(readOnly = true)
    fun getFullShifts(): List<ShiftEntity> {
        val upcomingShifts = shiftRepository.findUpcomingShifts()
        return upcomingShifts.filter { it.members.size >= it.maxMembers }
    }

    @Transactional(readOnly = true)
    fun getUpcomingShiftsByUserId(userId: Long) : List<ShiftEntity> {
        return shiftRepository.findUpcomingShiftsByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getAllShiftsByCookingClubId(cookingClubId : Long) : List<ShiftEntity> {
        return shiftRepository.findAllByCookingClubId(cookingClubId)
    }

    @Transactional(readOnly = true)
    fun getUpcomingShiftsByCookingClubId(cookingClubId : Long) : List<ShiftEntity> {
        return shiftRepository.findUpcomingShiftsByCookingClubId(cookingClubId)
    }

    @Transactional(readOnly = true)
    fun getMembersByShiftId(shiftId : Long) : List<UserEntity> {
        val shift = shiftRepository.findShiftEntityById(shiftId)
            ?: throw IllegalArgumentException("Shift with ID $shiftId not found")
        return userRepository.findAllById(shift.members).toList()
    }

    @Transactional(readOnly = true)
    fun getNewbiesByShiftId(shiftId : Long) : List<UserEntity> {
        val shift = shiftRepository.findShiftEntityById(shiftId)
            ?: throw IllegalArgumentException("Shift with ID $shiftId not found")
        return userRepository.findAllById(shift.newbies).toList()
    }

    @Transactional(readOnly = false)
    fun addMemberToShift(shiftId: Long, userId: Long): ShiftEntity {
        val shift = shiftRepository.findShiftEntityById(shiftId)
            ?: throw IllegalArgumentException("Shift with ID $shiftId not found")
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("User with ID $userId not found")

        if (shift.members.contains(userId)) {
            throw IllegalStateException("User with ID $userId is already a member of the shift $shiftId")
        }
        if (shift.members.size >= shift.maxMembers) {
            throw IllegalStateException("Shift with ID $shiftId has reached maximum member capacity")
        }

        val updatedMembers = shift.members.toMutableList().apply { add(userId) }
        shift.members = updatedMembers
        return shiftRepository.save(shift)
    }

    @Transactional(readOnly = false)
    fun addNewbieToShift(shiftId: Long, userId: Long): ShiftEntity {
        val shift = shiftRepository.findShiftEntityById(shiftId)
            ?: throw IllegalArgumentException("Shift with ID $shiftId not found")
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("User with ID $userId not found")

        if (shift.newbies.contains(userId)) {
            throw IllegalStateException("User with ID $userId is already a member of the shift $shiftId")
        }
        if (shift.newbies.size >= shift.members.size) {
            throw IllegalStateException("Shift with ID $shiftId does not have enough members for newbies to join")
        }

        val updatedNewbies = shift.newbies.toMutableList().apply { add(userId) }
        shift.newbies = updatedNewbies
        return shiftRepository.save(shift)
    }

    @Transactional(readOnly = false)
    fun removeMemberFromShift(shiftId: Long, userId: Long): ShiftEntity {
        val shift = shiftRepository.findShiftEntityById(shiftId)
            ?: throw IllegalArgumentException("Shift with ID $shiftId not found")
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("User with ID $userId not found")

        if (!shift.members.contains(userId)) {
            throw IllegalStateException("User with ID $userId is not a member of shift $shiftId")
        }

        val updatedMembers = shift.members.toMutableList().apply { remove(userId) }
        shift.members = updatedMembers
        return shiftRepository.save(shift)
    }

    @Transactional(readOnly = false)
    fun removeNewbieFromShift(shiftId: Long, userId: Long): ShiftEntity {
        val shift = shiftRepository.findShiftEntityById(shiftId)
            ?: throw IllegalArgumentException("Shift with ID $shiftId not found")
        val user = userRepository.findByIdOrNull(userId)
            ?: throw IllegalArgumentException("User with ID $userId not found")

        if (!shift.newbies.contains(userId)) {
            throw IllegalStateException("User with ID $userId is not a member of shift $shiftId")
        }

        val updatedNewbies = shift.newbies.toMutableList().apply { remove(userId) }
        shift.newbies = updatedNewbies
        return shiftRepository.save(shift)
    }

    @Transactional(readOnly = false)
    fun deleteShift(shiftId: Long) {
        shiftRepository.deleteById(shiftId)
    }

    @Transactional(readOnly = false)
    fun updateShift(shift: ShiftEntity): ShiftEntity {
        return shiftRepository.save(shift)
    }

    @Transactional(readOnly = false)
    fun createShiftFromFoodExRequest(request: CreateShiftFromRequestDTO) : List<ShiftEntity> {
        val foodExRequest = foodExService.getFoodExRequestsById(request.foodExRequestId)
            ?: throw RuntimeException("Food Ex Request with ID ${request.foodExRequestId} not found")

        if (foodExRequest.opening >= foodExRequest.closing) {
            throw IllegalArgumentException("Food Ex Request with ID ${request.foodExRequestId} start is after opening error")
        }

        val shifts = mutableListOf<ShiftEntity>()

        val lengthOfEachShift: Duration = Duration.between(foodExRequest.opening, foodExRequest.closing).dividedBy(request.numberOfShifts.toLong())

        for (i in 0..<request.numberOfShifts) {
            val shift = ShiftEntity(
                cookingClubId = foodExRequest.cookingClubId,
                maxMembers = request.maxMembers,
                opening = foodExRequest.opening.plus(lengthOfEachShift.multipliedBy(i.toLong())),
                closing = foodExRequest.opening.plus(lengthOfEachShift.multipliedBy((i + 1).toLong())),
                place = foodExRequest.place
            )
            shiftRepository.save(shift)
            shifts.add(shift)
        }

        foodExService.acceptFoodExRequest(request.foodExRequestId)

        return shifts
    }
}