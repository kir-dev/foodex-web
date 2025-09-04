package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.FoodEx_RequestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FoodEx_RequestRepository : JpaRepository<FoodEx_RequestEntity, Long> {
    fun findAllByCookingClubId(cookingClubId: Long): List<FoodEx_RequestEntity>
    fun findAllByUserId(userId: Long): List<FoodEx_RequestEntity>
    fun findAllByIsAcceptedTrue(): List<FoodEx_RequestEntity>
    fun findAllByIsAcceptedFalse(): List<FoodEx_RequestEntity>
}