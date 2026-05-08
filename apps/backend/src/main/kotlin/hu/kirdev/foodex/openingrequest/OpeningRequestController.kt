package hu.kirdev.foodex.openingrequest

import hu.kirdev.foodex.shift.CreateShiftFromOpeningRequestDto
import hu.kirdev.foodex.shift.DetailedShiftDto
import hu.kirdev.foodex.shift.ShiftService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api")
class OpeningRequestController(
    private val openingRequestService: OpeningRequestService,
    private val shiftService: ShiftService,
) {
    /********** REQUESTS *******************************************************************************/
    @Operation(summary = "Create an opening requests")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Opening requests created",
            content = [Content(schema = Schema(implementation = DetailedOpeningRequestDto::class))]
        )
    )
    @PostMapping("/requests")
    fun createOpeningRequest(@RequestBody createRequest: CreateOpeningRequestDto) : ResponseEntity<DetailedOpeningRequestDto> {
        val request = openingRequestService.createOpeningRequest(createRequest)
        return ResponseEntity.status(HttpStatus.OK).body(request)
    }


    /********** INCOMING-REQUESTS **********************************************************************/
    @Operation(summary = "List opening requests")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Opening requests found",
            content = [Content(schema = Schema(implementation = DetailedOpeningRequestDto::class))]
        )
    )
    @GetMapping("/incoming-requests")
    fun getUpcomingOpeningRequest() : ResponseEntity<List<DetailedOpeningRequestDto>> {
        val requests = openingRequestService.getUpcomingOpeningRequestsByIsAcceptedFalse()
        return ResponseEntity.status(HttpStatus.OK).body(requests)
    }

    @Operation(summary = "Get opening request")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Opening request found",
                content = [Content(schema = Schema(implementation = DetailedOpeningRequestDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Opening request not found"),
        ]
    )
    @GetMapping("/incoming-requests/{requestId}")
    fun getOpeningRequest(@PathVariable requestId: Int) : ResponseEntity<DetailedOpeningRequestDto> {
        val request = openingRequestService.getOpeningRequestById(requestId)
        return ResponseEntity.status(HttpStatus.OK).body(request)
    }

    @Operation(summary = "Update the opening request")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Opening request updated",
                content = [Content(schema = Schema(implementation = DetailedOpeningRequestDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Opening request not found"),
        ]
    )
    @PatchMapping("/incoming-requests/{requestId}")
    fun updateOpeningRequest(
        @PathVariable requestId: Int,
        @RequestBody toUpdate: UpdateOpeningRequestDto
        ) : ResponseEntity<DetailedOpeningRequestDto>  {

        val request = openingRequestService.updateOpeningRequest(requestId, toUpdate)
        return ResponseEntity.status(HttpStatus.OK).body(request)
    }

    @Operation(summary = "Delete the opening request")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Opening request deleted",
            ),
            ApiResponse(responseCode = "404", description = "Opening request not found"),
        ]
    )
    @DeleteMapping("/incoming-requests/{requestId}")
    fun deleteOpeningRequest(@PathVariable requestId: Int) : ResponseEntity<Void>  {
        openingRequestService.deleteOpeningRequest(requestId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @Operation(summary = "Create shifts from opening request")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Shifts created",
            content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
        )
    )
    @PostMapping("/requests/{requestId}")
    fun createShiftsFromOpeningRequest(
        @PathVariable requestId: Int,
        @RequestBody createRequest: CreateShiftFromOpeningRequestDto
        ) : ResponseEntity<List<DetailedShiftDto>> {

        val shifts = shiftService.createShiftsFromOpeningRequest(requestId, createRequest)
        return ResponseEntity.status(HttpStatus.OK).body(shifts)
    }
}