package hu.kirdev.foodex.dto

import jakarta.validation.constraints.*
import org.jetbrains.annotations.NotNull

data class CreateShiftFromRequestDTO(
    @field:NotNull
    val foodExRequestId: Int,

    @field:NotNull
    @field:Min(1)
    val maxMembers: Int,

    @field:NotNull
    @field:Min(1)
    val numberOfShifts: Int
)
