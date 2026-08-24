package hu.kirdev.foodex.admingrant

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateAdminGrantDto(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:NotBlank @field:Size(max = 36) val internalId: String,
)

data class UpdateAdminGrantDto(
    @field:NotBlank @field:Size(max = 100) val name: String,
    @field:NotBlank @field:Size(max = 36) val internalId: String,
)

data class AdminGrantDto(
    val id: Int,
    val name: String,
    val internalId: String,
) {
    constructor(grant: AdminGrantEntity) : this(
        id = grant.id,
        name = grant.name,
        internalId = grant.internalId,
    )
}
