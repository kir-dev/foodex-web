package hu.kirdev.foodex.dto

import hu.kirdev.foodex.model.FoodExRequestEntity
import hu.kirdev.foodex.model.ShiftEntity

data class OrdersResponseDTO(
    val incomingFoodExRequests: List<FoodExRequestEntity>,
    val acceptedShifts: List<ShiftEntity>
)
