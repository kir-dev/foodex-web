package hu.kirdev.foodex.shift

import hu.kirdev.foodex.cookingclub.CookingClubDto
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserDto
import java.time.LocalDateTime

data class CreateShiftDto (
    val cookingClubId: Int,
    val maxMembers: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val comment: String,
)

data class UpdateShiftDto (
    val cookingClubId: Int?,
    val maxMembers: Int?,
    val opening: LocalDateTime?,
    val closing: LocalDateTime?,
    val place: String?,
    val comment: String?,
)

data class ShiftDto (
    val id: Int,
    val cookingClubId: Int,
    val maxMembers: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val comment: String,
) {
    constructor(shift: ShiftEntity) : this(
        id = shift.id,
        cookingClubId = shift.cookingClub.id,
        maxMembers = shift.maxMembers,
        opening = shift.opening,
        closing = shift.closing,
        place = shift.place,
        comment = shift.comment,
    )
}

data class DetailedShiftDto (
    val id: Int,
    val cookingClub: CookingClubDto,
    val maxMembers: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val comment: String,
    val members: List<UserDto>,
    val newbies: List<UserDto>,
) {
    constructor(shift: ShiftEntity) : this(
        id = shift.id,
        cookingClub = CookingClubDto(shift.cookingClub),
        maxMembers = shift.maxMembers,
        opening = shift.opening,
        closing = shift.closing,
        place = shift.place,
        comment = shift.comment,
        members = shift.workers.filter { it.role == Role.MEMBER || it.role == Role.ADMIN }.map { UserDto(it) },
        newbies = shift.workers.filter { it.role == Role.NEWBIE }.map { UserDto(it) },
    )
}

data class CreateShiftFromOpeningRequestDto (
    val openingRequestId: Int,
    val maxMembers: Int,
    val numberOfShifts: Int,
)

data class ShiftsResponseDto (
    val activeShifts: List<DetailedShiftDto>,
    val fullShifts: List<DetailedShiftDto>,
)