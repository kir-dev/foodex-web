package hu.kirdev.foodex.trialgrant

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
@RequestMapping("/api/trial-grants")
class TrialGrantController(
    private val trialGrantService: TrialGrantService,
) {

    @Operation(summary = "List trial grants")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Trial grants found",
            content = [Content(schema = Schema(implementation = TrialGrantDto::class))]
        )
    )
    @GetMapping
    fun getTrialGrants(): ResponseEntity<List<TrialGrantDto>> {
        return ResponseEntity.ok(trialGrantService.getAllGrants())
    }

    @Operation(summary = "Create a trial grant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Trial grant created",
                content = [Content(schema = Schema(implementation = TrialGrantDto::class))]
            ),
            ApiResponse(responseCode = "409", description = "internalId already granted"),
        ]
    )
    @PostMapping
    fun createTrialGrant(
        @Valid @RequestBody request: CreateTrialGrantDto
    ): ResponseEntity<TrialGrantDto> {
        val grant = trialGrantService.createGrant(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(grant)
    }

    @Operation(summary = "Update a trial grant")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Trial grant updated",
                content = [Content(schema = Schema(implementation = TrialGrantDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Trial grant not found"),
            ApiResponse(responseCode = "409", description = "internalId already granted"),
        ]
    )
    @PutMapping("/{grantId}")
    fun updateTrialGrant(
        @PathVariable grantId: Int,
        @Valid @RequestBody request: UpdateTrialGrantDto
    ): ResponseEntity<TrialGrantDto> {
        val grant = trialGrantService.updateGrant(grantId, request)
        return ResponseEntity.ok(grant)
    }

    @Operation(summary = "Delete a trial grant")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Trial grant deleted"),
            ApiResponse(responseCode = "404", description = "Trial grant not found"),
        ]
    )
    @DeleteMapping("/{grantId}")
    fun deleteTrialGrant(@PathVariable grantId: Int): ResponseEntity<Void> {
        trialGrantService.deleteGrant(grantId)
        return ResponseEntity.noContent().build()
    }
}
