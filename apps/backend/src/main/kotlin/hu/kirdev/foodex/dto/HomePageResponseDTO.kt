package hu.kirdev.foodex.dto

import hu.kirdev.foodex.model.ShiftEntity
import hu.kirdev.foodex.model.UserEntity

data class HomePageResponseDTO(
    val feelingOfTheWeek: String,
    val foodExLogo: String,
    val activeMembers: List<UserEntity>,
    val upcomingShifts: List<ShiftEntity>
)
