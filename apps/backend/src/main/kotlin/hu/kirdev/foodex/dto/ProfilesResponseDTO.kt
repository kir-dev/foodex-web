package hu.kirdev.foodex.dto

import hu.kirdev.foodex.model.FoodExRequestEntity
import hu.kirdev.foodex.model.Role
import hu.kirdev.foodex.model.ShiftEntity
import org.springframework.http.RequestEntity

data class ProfilesResponseDTO(
    val userId: Long,
    val role: Role,
    val name: String,
    val nickname: String,
    val email: String,
    val favouriteQuote: String,
    val isActive: Boolean,
    val profilePicture: String?,
    val shifts: List<ShiftEntity>,
    val requests: List<FoodExRequestEntity>
)
