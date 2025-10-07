package hu.kirdev.foodex.dto

import org.jetbrains.annotations.NotNull

data class ApplyForShiftDTO(
    @field:NotNull val userId: Int,
    @field:NotNull val shiftId: Int
)
