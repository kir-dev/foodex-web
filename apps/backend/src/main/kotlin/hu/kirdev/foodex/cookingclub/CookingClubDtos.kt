package hu.kirdev.foodex.cookingclub

import hu.kirdev.foodex.openingrequest.OpeningRequestDto
import hu.kirdev.foodex.shift.ShiftDto
import hu.kirdev.foodex.user.UserDto

data class CreateCookingClubDto (
    val id: Int,
    val name: String,
)

data class UpdateCookingClubDto (
    val id: Int,
    val name: String,
)

data class CookingClubDto (
    val id: Int,
    val name: String,
) {
    constructor(cookingClub: CookingClubEntity) : this(
        id = cookingClub.id,
        name = cookingClub.name,
    )
}

data class DetailedCookingClubDto (
    val id: Int,
    val name: String,
    val leaders: List<UserDto>,
    val shifts: List<ShiftDto>,
    val requests: List<OpeningRequestDto>,
) {
    constructor(cookingClub: CookingClubEntity) : this(
        id = cookingClub.id,
        name = cookingClub.name,
        leaders = cookingClub.leaders.map { UserDto(it) },
        shifts = cookingClub.shifts.map { ShiftDto(it) },
        requests = cookingClub.requests.map { OpeningRequestDto(it) },
    )
}