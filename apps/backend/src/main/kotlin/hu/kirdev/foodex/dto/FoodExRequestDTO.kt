package hu.kirdev.foodex.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class FoodExRequestDTO(
    @field:NotBlank
    val userId: Long,

    @field:NotBlank
    val cookingClubId: Long,

    @field:NotBlank
    val opening: LocalDateTime,

    @field:NotBlank
    val closing: LocalDateTime,

    @field:NotBlank
    @field:Size(max = 30)
    val place: String,

    @field:Size(max = 255)
    val description: String
)
