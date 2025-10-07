package hu.kirdev.foodex.dto

import hu.kirdev.foodex.model.ShiftEntity

data class ShiftsResponseDTO(
    val activeShifts: List<ShiftEntity>,
    val fullShifts: List<ShiftEntity>
)
