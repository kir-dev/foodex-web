package hu.kirdev.foodex.dto

import jakarta.validation.constraints.*

data class CreateShiftFromRequestDTO(
    @field:NotBlank
    val foodExRequestId: Int,

    @field:NotBlank
    @field:Min(1)
    val maxMembers: Int,

    @field:NotBlank
    @field:Min(1)
    val numberOfShifts: Int
)
