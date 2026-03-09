package hu.kirdev.foodex.dto

import hu.kirdev.foodex.model.OpeningRequestEntity
import hu.kirdev.foodex.model.ShiftEntity

data class OrdersResponseDTO(
    val incomingOpeningRequests: List<OpeningRequestEntity>,
    val acceptedShifts: List<ShiftEntity>
)
