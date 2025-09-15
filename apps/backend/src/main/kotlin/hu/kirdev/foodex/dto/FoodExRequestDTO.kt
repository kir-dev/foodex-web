package hu.kirdev.foodex.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class FoodExRequestDTO(
    @field:NotNull
    val userId: Long,

    @field:NotNull
    val cookingClubId: Long,

    val opening: LocalDateTime,

    val closing: LocalDateTime,

    @field:NotBlank
    @field:Size(max = 30)
    val place: String,

    @field:Size(max = 255)
    val description: String
)
