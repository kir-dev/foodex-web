package hu.kirdev.foodex.openingrequest

import hu.kirdev.foodex.cookingclub.CookingClubDto
import hu.kirdev.foodex.shift.ShiftDto
import hu.kirdev.foodex.user.UserDto
import java.time.LocalDateTime

data class CreateOpeningRequestDto (
    val userId: Int,
    val cookingClubId: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val description: String,
)

data class UpdateOpeningRequestDto (
    val isAccepted: Boolean,
    val userId: Int,
    val cookingClubId: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val description: String,
)

data class OpeningRequestDto (
    val id: Int,
    val isAccepted: Boolean,
    val userId: Int,
    val cookingClubId: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val description: String,
) {
    constructor(openingRequest: OpeningRequestEntity) : this(
        id = openingRequest.id,
        isAccepted = openingRequest.isAccepted,
        userId = openingRequest.user.id,
        cookingClubId = openingRequest.cookingClub.id,
        opening = openingRequest.opening,
        closing = openingRequest.closing,
        place = openingRequest.place,
        description = openingRequest.description,
    )
}

data class DetailedOpeningRequestDto (
    val id: Int,
    val isAccepted: Boolean,
    val user: UserDto,
    val cookingClub: CookingClubDto,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val description: String,
) {
    constructor(openingRequest: OpeningRequestEntity) : this(
        id = openingRequest.id,
        isAccepted = openingRequest.isAccepted,
        user = UserDto(openingRequest.user),
        cookingClub = CookingClubDto(openingRequest.cookingClub),
        opening = openingRequest.opening,
        closing = openingRequest.closing,
        place = openingRequest.place,
        description = openingRequest.description,
    )
}

data class OrdersResponseDto (
    val incomingRequests: List<OpeningRequestDto>,
    val acceptedShifts: List<ShiftDto>
)