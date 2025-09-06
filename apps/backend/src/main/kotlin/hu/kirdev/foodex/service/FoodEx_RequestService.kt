package hu.kirdev.foodex.service

import hu.kirdev.foodex.dto.FoodEx_RequestDTO
import hu.kirdev.foodex.model.FoodEx_RequestEntity
import hu.kirdev.foodex.repository.FoodEx_RequestRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FoodEx_RequestService(private val foodEx_RequestRepository: FoodEx_RequestRepository) {

    @Transactional(readOnly = true)
    fun getAllFoodEx_Requests() : List<FoodEx_RequestEntity> {
        return foodEx_RequestRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsById(id: Long) : FoodEx_RequestEntity? {
        return foodEx_RequestRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsByUserId(userId: Long) : List<FoodEx_RequestEntity> {
        return foodEx_RequestRepository.findAllByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsByCookingClubId(cookingClubId: Long) : List<FoodEx_RequestEntity> {
        return foodEx_RequestRepository.findAllByCookingClubId(cookingClubId)
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsByIsAcceptedTrue() : List<FoodEx_RequestEntity> {
        return foodEx_RequestRepository.findAllByIsAcceptedTrue()
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsByIsAcceptedFalse() : List<FoodEx_RequestEntity> {
        return foodEx_RequestRepository.findAllByIsAcceptedFalse()
    }

    @Transactional(readOnly = false)
    fun createFoodEx_Request(foodExRequest: FoodEx_RequestDTO) : FoodEx_RequestEntity {
        val request = FoodEx_RequestEntity(
            userId = foodExRequest.userId,
            cookingClubId = foodExRequest.cookingClubId,
            opening = foodExRequest.opening,
            closing = foodExRequest.closing,
            place = foodExRequest.place,
            description = foodExRequest.description
        )
        return foodEx_RequestRepository.save(request)
    }

    @Transactional(readOnly = false)
    fun acceptFoodEx_Request(id: Long) : FoodEx_RequestEntity? {
        return foodEx_RequestRepository.findByIdOrNull(id)?.also {
            it.isAccepted = true
            foodEx_RequestRepository.save(it)
        }
    }

    @Transactional(readOnly = false)
    fun deleteFoodEx_Request(id: Long) {
        foodEx_RequestRepository.deleteById(id)
    }
}