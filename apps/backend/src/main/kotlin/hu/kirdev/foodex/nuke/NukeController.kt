package hu.kirdev.foodex.nuke

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/nuke")
class NukeController(
    private val nukeService: NukeService,
) {

    @Operation(summary = "Delete all opening requests, openings, and shifts")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "All opening requests and shifts deleted"),
    )
    @DeleteMapping
    fun nuke(): ResponseEntity<Void> {
        nukeService.deleteAllOpeningRequestsAndShifts()
        return ResponseEntity.noContent().build()
    }
}
