package hu.kirdev.foodex.newbiegrant

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
@RequestMapping("/api/newbie-grants")
class NewbieGrantController(
    private val newbieGrantService: NewbieGrantService,
) {

    @Operation(summary = "List newbie grants")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Newbie grants found",
            content = [Content(schema = Schema(implementation = NewbieGrantDto::class))]
        )
    )
    @GetMapping
    fun getNewbieGrants(): ResponseEntity<List<NewbieGrantDto>> {
        return ResponseEntity.ok(newbieGrantService.getAllGrants())
    }

    @Operation(summary = "Create a newbie grant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Newbie grant created",
                content = [Content(schema = Schema(implementation = NewbieGrantDto::class))]
            ),
            ApiResponse(responseCode = "409", description = "internalId already granted"),
        ]
    )
    @PostMapping
    fun createNewbieGrant(
        @Valid @RequestBody request: CreateNewbieGrantDto
    ): ResponseEntity<NewbieGrantDto> {
        val grant = newbieGrantService.createGrant(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(grant)
    }

    @Operation(summary = "Update a newbie grant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Newbie grant updated",
                content = [Content(schema = Schema(implementation = NewbieGrantDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Newbie grant not found"),
            ApiResponse(responseCode = "409", description = "internalId already granted"),
        ]
    )
    @PutMapping("/{grantId}")
    fun updateNewbieGrant(
        @PathVariable grantId: Int,
        @Valid @RequestBody request: UpdateNewbieGrantDto
    ): ResponseEntity<NewbieGrantDto> {
        val grant = newbieGrantService.updateGrant(grantId, request)
        return ResponseEntity.ok(grant)
    }

    @Operation(summary = "Delete a newbie grant")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Newbie grant deleted"),
            ApiResponse(responseCode = "404", description = "Newbie grant not found"),
        ]
    )
    @DeleteMapping("/{grantId}")
    fun deleteNewbieGrant(@PathVariable grantId: Int): ResponseEntity<Void> {
        newbieGrantService.deleteGrant(grantId)
        return ResponseEntity.noContent().build()
    }
}
