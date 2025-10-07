package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.FoodExRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FoodExRequestRepository : JpaRepository<FoodExRequestEntity, Int> {
    fun findAllByCookingClubId(cookingClubId: Int): List<FoodExRequestEntity>
    fun findAllByUserId(userId: Int): List<FoodExRequestEntity>
    fun findAllByIsAcceptedTrue(): List<FoodExRequestEntity>
    fun findAllByIsAcceptedFalse(): List<FoodExRequestEntity>
}