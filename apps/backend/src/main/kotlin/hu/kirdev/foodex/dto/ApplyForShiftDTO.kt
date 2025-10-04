package hu.kirdev.foodex.dto
import jakarta.validation.constraints.NotBlank

data class ApplyForShiftDTO(
    @field:NotBlank val userId: Long,
    @field:NotBlank val shiftId: Long,
    @field:NotBlank val action: String
)
