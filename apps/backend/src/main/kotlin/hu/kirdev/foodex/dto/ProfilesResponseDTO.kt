package hu.kirdev.foodex.dto

import hu.kirdev.foodex.model.OpeningRequestEntity
import hu.kirdev.foodex.model.Role
import hu.kirdev.foodex.model.ShiftEntity

data class ProfilesResponseDTO(
    val userId: Int,
    val role: Role,
    val name: String,
    val nickname: String,
    val email: String,
    val favouriteQuote: String,
    val isActive: Boolean,
    val profilePicture: String?,
    val shifts: List<ShiftEntity>,
    val requests: List<OpeningRequestEntity>
)
