package hu.kirdev.foodex.shift

import hu.kirdev.foodex.cookingclub.CookingClubDto
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserDto
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class CreateShiftDto(
    val cookingClubId: Int,
    @field:Positive val openingRequestId: Int,
    @field:Positive @field:Max(6) val maxMembers: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    @field:NotBlank val place: String,
    val comment: String = "",
)

data class UpdateShiftDto(
    val cookingClubId: Int?,
    @field:Positive @field:Max(6) val maxMembers: Int?,
    val opening: LocalDateTime?,
    val closing: LocalDateTime?,
    val place: String?,
    val comment: String?,
)

data class ShiftDto(
    val id: Int,
    val cookingClubId: Int,
    val maxMembers: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val comment: String,
    val openingRequestId: Int?,
) {
    constructor(shift: ShiftEntity) : this(
        id = shift.id,
        cookingClubId = shift.cookingClub.id,
        maxMembers = shift.maxMembers,
        opening = shift.opening,
        closing = shift.closing,
        place = shift.place,
        comment = shift.comment,
        openingRequestId = shift.openingRequest?.id,
    )
}

data class DetailedShiftDto(
    val id: Int,
    val cookingClub: CookingClubDto,
    val maxMembers: Int,
    val opening: LocalDateTime,
    val closing: LocalDateTime,
    val place: String,
    val comment: String,
    val openingRequestId: Int?,
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
        openingRequestId = shift.openingRequest?.id,
        members = shift.workers.filter { it.role == Role.MEMBER || it.role == Role.ADMIN }.map { UserDto(it) },
        newbies = shift.workers.filter { it.role == Role.NEWBIE }.map { UserDto(it) },
    )
}

data class CreateShiftFromOpeningRequestDto(
    @field:Positive @field:Max(6) val maxMembers: Int,
    @field:Positive @field:Max(4) val numberOfShifts: Int,
)

data class ActiveAndFullShifts(
    val activeShifts: List<DetailedShiftDto>,
    val fullShifts: List<DetailedShiftDto>,
)
