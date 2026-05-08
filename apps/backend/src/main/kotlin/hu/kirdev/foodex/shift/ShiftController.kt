package hu.kirdev.foodex.shift

import hu.kirdev.foodex.openingrequest.OpeningRequestService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ShiftController(
    private val shiftService: ShiftService,
    private val openingRequestService: OpeningRequestService,
) {
    /********** OPENINGS *******************************************************************************/
    @Operation(summary = "List all shifts of semester")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Shifts found",
            content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
        )
    )
    @GetMapping("/openings")
    fun getOpenings() : ResponseEntity<List<DetailedShiftDto>> {
        val openings = shiftService.getAllShiftsInSemester()
        return ResponseEntity.status(HttpStatus.OK).body(openings)
    }

    @Operation(summary = "Create a shift")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Shift found",
                content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Cooking club not found"),
        ]
    )
    @PostMapping("/openings")
    fun createShift(@RequestBody createRequest: CreateShiftDto) : ResponseEntity<DetailedShiftDto> {
        val shift = shiftService.createShift(createRequest)
        return ResponseEntity.status(HttpStatus.OK).body(shift)
    }

    @Operation(summary = "Update a shift")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Shift updated",
                content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Shift not found"),
        ]
    )
    @PatchMapping("/openings/{shiftId}")
    fun updateShift(@PathVariable shiftId: Int, @RequestBody updateShift: UpdateShiftDto) : ResponseEntity<DetailedShiftDto> {
        val shift = shiftService.updateShift(shiftId, updateShift)
        return ResponseEntity.status(HttpStatus.OK).body(shift)
    }

    @Operation(summary = "Delete a shift")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Shift deleted",
            ),
            ApiResponse(responseCode = "404", description = "Shift not found"),
        ]
    )
    @DeleteMapping("/openings/{shiftId}")
    fun deleteShift(@PathVariable shiftId: Int) : ResponseEntity<Void> {
        shiftService.deleteShift(shiftId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }


    /********** SHIFTS *********************************************************************************/
    @Operation(summary = "List upcoming active and full shifts")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Shifts found",
            content = [Content(schema = Schema(implementation = ActiveAndFullShifts::class))]
        )
    )
    @GetMapping("/shifts")
    fun getUpcomingActiveAndFullShifts() : ResponseEntity<ActiveAndFullShifts> {
        val shifts = shiftService.getUpcomingActiveAndFullShifts()
        return ResponseEntity.status(HttpStatus.OK).body(shifts)
    }

    @Operation(summary = "Get shift")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Shift found",
                content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Shift not found"),
        ]
    )
    @GetMapping("/shifts/{shiftId}")
    fun getShift(@PathVariable shiftId: Int) : ResponseEntity<DetailedShiftDto> {
        val shift = shiftService.getShiftById(shiftId)
        return ResponseEntity.status(HttpStatus.OK).body(shift)
    }

    @Operation(summary = "Add worker to shift")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "worker added to shift",
                content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Shift or worker not found"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @PostMapping("/shifts/{shiftId}/{workerId}")
    fun addWorkerToShift(@PathVariable shiftId: Int, @PathVariable workerId: Int) : ResponseEntity<DetailedShiftDto> {
        val shift = shiftService.addWorkerToShift(workerId, shiftId)
        return ResponseEntity.status(HttpStatus.OK).body(shift)
    }

    @Operation(summary = "Remove worker from shift")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "worker removed from shift",
                content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Shift or worker not found"),
            ApiResponse(responseCode = "400", description = "Bad request"),
        ]
    )
    @DeleteMapping("/shifts/{shiftId}/{workerId}")
    fun removeWorkerFromShift(@PathVariable shiftId: Int, @PathVariable workerId: Int) : ResponseEntity<DetailedShiftDto> {
        val shift = shiftService.removeWorkerFromShift(workerId, shiftId)
        return ResponseEntity.status(HttpStatus.OK).body(shift)
    }
}