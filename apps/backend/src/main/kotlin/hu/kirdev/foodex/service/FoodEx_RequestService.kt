package hu.kirdev.foodex.service

import hu.kirdev.foodex.model.FoodEx_RequestEntity
import hu.kirdev.foodex.repository.FoodEx_RequestRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FoodEx_RequestService(private val foodexRequestrepository: FoodEx_RequestRepository) {

    @Transactional(readOnly = true)
    fun getAllFoodEx_Requests() : List<FoodEx_RequestEntity> {
        return foodexRequestrepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsById(id: Long) : FoodEx_RequestEntity? {
        return foodexRequestrepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsByUserId(userId: Long) : List<FoodEx_RequestEntity> {
        return foodexRequestrepository.findAllByUserId(userId)
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsByCookingClubId(cookingClubId: Long) : List<FoodEx_RequestEntity> {
        return foodexRequestrepository.findAllByCookingClubId(cookingClubId)
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsByIsAcceptedTrue() : List<FoodEx_RequestEntity> {
        return foodexRequestrepository.findAllByIsAcceptedTrue()
    }

    @Transactional(readOnly = true)
    fun getFoodEx_RequestsByIsAcceptedFalse() : List<FoodEx_RequestEntity> {
        return foodexRequestrepository.findAllByIsAcceptedFalse()
    }

    @Transactional(readOnly = false)
    fun acceptFoodEx_Request(id: Long) : FoodEx_RequestEntity? {
        var request = foodexRequestrepository.findByIdOrNull(id)?.let {}
        return request
    }
}