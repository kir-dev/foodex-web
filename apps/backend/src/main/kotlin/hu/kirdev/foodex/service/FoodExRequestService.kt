package hu.kirdev.foodex.service

import hu.kirdev.foodex.dto.FoodExRequestDTO
import hu.kirdev.foodex.model.FoodExRequestEntity
import hu.kirdev.foodex.repository.FoodExRequestRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FoodExRequestService(private val foodExRequestRepository: FoodExRequestRepository) {

    @Transactional(readOnly = true)
    fun getAllFoodExRequests() : List<FoodExRequestEntity> {
        return foodExRequestRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsById(id: Long) : FoodExRequestEntity? {
        return foodExRequestRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsByUserId(userId: Long) : List<FoodExRequestEntity> {
        return foodExRequestRepository.findAllByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsByCookingClubId(cookingClubId: Long) : List<FoodExRequestEntity> {
        return foodExRequestRepository.findAllByCookingClubId(cookingClubId)
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsByIsAcceptedTrue() : List<FoodExRequestEntity> {
        return foodExRequestRepository.findAllByIsAcceptedTrue()
    }

    @Transactional(readOnly = true)
    fun getFoodExRequestsByIsAcceptedFalse() : List<FoodExRequestEntity> {
        return foodExRequestRepository.findAllByIsAcceptedFalse()
    }

    @Transactional(readOnly = false)
    fun createFoodExRequest(foodExRequest: FoodExRequestDTO) : FoodExRequestEntity {
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
    fun acceptFoodExRequest(id: Long) : FoodExRequestEntity? {
        return foodExRequestRepository.findByIdOrNull(id)?.also {
            it.isAccepted = true
            foodExRequestRepository.save(it)
        }
    }

    @Transactional(readOnly = false)
    fun deleteFoodExRequest(id: Long) {
        foodExRequestRepository.deleteById(id)
    }
}