package hu.kirdev.foodex.trialgrant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTrialGrantDto(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:NotBlank @field:Size(max = 36) val internalId: String,
)

data class UpdateTrialGrantDto(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:NotBlank @field:Size(max = 36) val internalId: String,
)

data class TrialGrantDto(
    val id: Int,
    val name: String,
    val internalId: String,
) {
    constructor(grant: TrialGrantEntity) : this(
        id = grant.id,
        name = grant.name,
        internalId = grant.internalId,
    )
}
