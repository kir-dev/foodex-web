package hu.kirdev.foodex.service

import hu.kirdev.foodex.dto.FoodExRequestDTO
import hu.kirdev.foodex.model.FoodExRequestEntity
import hu.kirdev.foodex.repository.FoodExRequestRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class FoodExRequestService(
    private val foodExRequestRepository: FoodExRequestRepository,
    private val cookingClubService: CookingClubService,
    private val configurationService: ConfigurationService
) {

    @Transactional(readOnly = true)
    fun getAllFoodExRequests() : List<FoodExRequestEntity> {
        return foodExRequestRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsById(id: Int) : FoodExRequestEntity? {
        return foodExRequestRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsByUserId(userId: Int) : List<FoodExRequestEntity> {
        val startOfSemester = configurationService.get().startOfSemester
        return foodExRequestRepository.findAllByUserId(userId).filter { it.closing > startOfSemester }
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsByCookingClubId(cookingClubId: Int) : List<FoodExRequestEntity> {
        return foodExRequestRepository.findAllByCookingClubId(cookingClubId)
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsByIsAcceptedTrue() : List<FoodExRequestEntity> {
        return foodExRequestRepository.findAllByIsAcceptedTrue()
    }

    @Transactional(readOnly = true)
    fun getRelevantFoodExRequestsByIsAcceptedFalse() : List<FoodExRequestEntity> {
        val now = LocalDateTime.now()
        return foodExRequestRepository.findAllByIsAcceptedFalse().filter { it.opening > now }
    }

    @Transactional(readOnly = false)
    fun createFoodExRequest(foodExRequest: FoodExRequestDTO) : FoodExRequestEntity {

        if (!cookingClubService.isLeaderOfCookingClub(foodExRequest.userId, foodExRequest.cookingClubId)) {
            throw IllegalArgumentException("User with id ${foodExRequest.userId} is NOT a LEADER of CookingClub with id ${foodExRequest.cookingClubId}")
        }

        if (foodExRequest.opening >= foodExRequest.closing) {
            throw IllegalArgumentException("Closing must be after opening")
        }
        
        val request = FoodExRequestEntity(
            userId = foodExRequest.userId,
            cookingClubId = foodExRequest.cookingClubId,
            opening = foodExRequest.opening,
            closing = foodExRequest.closing,
            place = foodExRequest.place,
            description = foodExRequest.description
        )
        return foodExRequestRepository.save(request)
    }

    @Transactional(readOnly = false)
    fun acceptFoodExRequest(id: Int) : FoodExRequestEntity? {
        return foodExRequestRepository.findByIdOrNull(id)?.also {
            it.isAccepted = true
            foodExRequestRepository.save(it)
        }
    }

    @Transactional(readOnly = false)
    fun deleteFoodExRequest(id: Int) {
        foodExRequestRepository.deleteById(id)
    }
}