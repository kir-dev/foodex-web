package hu.kirdev.foodex.dto

import jakarta.validation.constraints.*
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

data class FoodExRequestDTO(
    @field:NotNull
    val userId: Int,

    @field:NotNull
    val cookingClubId: Int,

    @field:NotNull
    val opening: LocalDateTime,

    @field:NotNull
    val closing: LocalDateTime,

    @field:NotBlank
    @field:Size(max = 30)
    val place: String,

    @field:Size(max = 255)
    val description: String
)
