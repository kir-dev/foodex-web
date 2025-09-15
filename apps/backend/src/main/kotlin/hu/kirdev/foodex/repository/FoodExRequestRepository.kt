package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.FoodExRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FoodExRequestRepository : JpaRepository<FoodExRequestEntity, Long> {
    fun findAllByCookingClubId(cookingClubId: Long): List<FoodExRequestEntity>
    fun findAllByUserId(userId: Long): List<FoodExRequestEntity>
    fun findAllByIsAcceptedTrue(): List<FoodExRequestEntity>
    fun findAllByIsAcceptedFalse(): List<FoodExRequestEntity>
}