package hu.kirdev.foodex.newbiegrant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateNewbieGrantDto(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:NotBlank @field:Size(max = 36) val internalId: String,
)

data class UpdateNewbieGrantDto(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:NotBlank @field:Size(max = 36) val internalId: String,
)

data class NewbieGrantDto(
    val id: Int,
    val name: String,
    val internalId: String,
) {
    constructor(grant: NewbieGrantEntity) : this(
        id = grant.id,
        name = grant.name,
        internalId = grant.internalId,
    )
}
