package hu.kirdev.foodex.admingrant

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin-grants")
class AdminGrantController(
    private val adminGrantService: AdminGrantService,
) {

    @Operation(summary = "List admin grants")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Admin grants found",
            content = [Content(schema = Schema(implementation = AdminGrantDto::class))]
        )
    )
    @GetMapping
    fun getAdminGrants(): ResponseEntity<List<AdminGrantDto>> {
        return ResponseEntity.ok(adminGrantService.getAllGrants())
    }

    @Operation(summary = "Create an admin grant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Admin grant created",
                content = [Content(schema = Schema(implementation = AdminGrantDto::class))]
            ),
            ApiResponse(responseCode = "409", description = "internalId already granted"),
        ]
    )
    @PostMapping
    fun createAdminGrant(
        @Valid @RequestBody request: CreateAdminGrantDto
    ): ResponseEntity<AdminGrantDto> {
        val grant = adminGrantService.createGrant(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(grant)
    }

    @Operation(summary = "Update an admin grant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Admin grant updated",
                content = [Content(schema = Schema(implementation = AdminGrantDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Admin grant not found"),
            ApiResponse(responseCode = "409", description = "internalId already granted"),
        ]
    )
    @PutMapping("/{grantId}")
    fun updateAdminGrant(
        @PathVariable grantId: Int,
        @Valid @RequestBody request: UpdateAdminGrantDto
    ): ResponseEntity<AdminGrantDto> {
        val grant = adminGrantService.updateGrant(grantId, request)
        return ResponseEntity.ok(grant)
    }

    @Operation(summary = "Delete an admin grant")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Admin grant deleted"),
            ApiResponse(responseCode = "404", description = "Admin grant not found"),
        ]
    )
    @DeleteMapping("/{grantId}")
    fun deleteAdminGrant(@PathVariable grantId: Int): ResponseEntity<Void> {
        adminGrantService.deleteGrant(grantId)
        return ResponseEntity.noContent().build()
    }
}
