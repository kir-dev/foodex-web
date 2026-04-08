package hu.kirdev.foodex.user

import hu.kirdev.foodex.cookingclub.CookingClubDto
import hu.kirdev.foodex.openingrequest.OpeningRequestDto
import hu.kirdev.foodex.shift.ShiftDto

data class CreatUserDto (
    val role: Role,
    val name: String,
    val nickname: String?,
    val email: String,
    val favouriteQuote: String?,
    val isActive: Boolean,
    val profilePicture: String?,
)

data class UpdateUserDto (
    val name: String,
    val nickname: String?,
    val email: String,
    val favouriteQuote: String?,
    val profilePicture: String?,
)

data class UserDto (
    val id: Int,
    val role: Role,
    val nickname: String,
) {
    constructor(user: UserEntity) : this(
        id = user.id,
        role = user.role,
        nickname = user.nickname ?: user.name,
    )
}

data class DetailedUserDto (
    val id: Int,
    val role: Role,
    val name: String,
    val nickname: String?,
    val email: String,
    val favouriteQuote: String?,
    val isActive: Boolean,
    val profilePicture: String?,
    val leaderAt: List<CookingClubDto>,
    val shifts: List<ShiftDto>,
    val requests: List<OpeningRequestDto>,
) {
    constructor(user: UserEntity) : this(
        id = user.id,
        role = user.role,
        name = user.name,
        nickname = user.nickname ?: user.name,
        email = user.email,
        favouriteQuote = user.favouriteQuote,
        isActive = user.isActive,
        profilePicture = user.profilePicture,
        leaderAt = user.leaderAt.map { CookingClubDto(it) },
        shifts = user.shifts.map { ShiftDto(it) },
        requests = user.requests.map { OpeningRequestDto(it) },
    )
}