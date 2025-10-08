package hu.kirdev.foodex.dto

import org.jetbrains.annotations.NotNull

data class CookingClubLeaderDTO(
    @field:NotNull val clubId: Int,
    @field:NotNull val leaderId: Int
)
