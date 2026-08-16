package hu.kirdev.foodex.shift

import hu.kirdev.foodex.security.CurrentUserService
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ShiftController(
    private val shiftService: ShiftService,
    private val currentUserService: CurrentUserService,
) {

    @Operation(summary = "List all shifts of semester")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Shifts found",
            content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
        )
    )
    @GetMapping("/semester-shifts")
    fun getSemesterShifts(): ResponseEntity<List<DetailedShiftDto>> {
        val shifts = shiftService.getAllShiftsInSemester()
        return ResponseEntity.status(HttpStatus.OK).body(shifts)
    }

    @Operation(summary = "Create a shift")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Shift created",
                content = [Content(schema = Schema(implementation = DetailedShiftDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Cooking club not found"),
        ]
    )
    @PostMapping("/semester-shifts")
    fun createShift(@Valid @RequestBody createRequest: CreateShiftDto): ResponseEntity<DetailedShiftDto> {
        val actor = currentUserService.requireUser()
        val shift = shiftService.createShift(createRequest, actor)
        return ResponseEntity.status(HttpStatus.CREATED).body(shift)
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
    @PatchMapping("/semester-shifts/{shiftId}")
    fun updateShift(
        @PathVariable shiftId: Int,
        @Valid @RequestBody updateShift: UpdateShiftDto
    ): ResponseEntity<DetailedShiftDto> {
        val actor = currentUserService.requireUser()
        val shift = shiftService.updateShift(shiftId, updateShift, actor)
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
    @DeleteMapping("/semester-shifts/{shiftId}")
    fun deleteShift(@PathVariable shiftId: Int): ResponseEntity<Void> {
        val actor = currentUserService.requireUser()
        shiftService.deleteShift(shiftId, actor)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @Operation(summary = "List upcoming active and full shifts")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Shifts found",
            content = [Content(schema = Schema(implementation = ActiveAndFullShifts::class))]
        )
    )
    @GetMapping("/shifts")
    fun getUpcomingActiveAndFullShifts(): ResponseEntity<ActiveAndFullShifts> {
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
    fun getShift(@PathVariable shiftId: Int): ResponseEntity<DetailedShiftDto> {
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
    fun addWorkerToShift(
        @PathVariable shiftId: Int,
        @PathVariable workerId: Int
    ): ResponseEntity<DetailedShiftDto> {
        val actor = currentUserService.requireUser()
        val shift = shiftService.addWorkerToShift(workerId, shiftId, actor)
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
    fun removeWorkerFromShift(
        @PathVariable shiftId: Int,
        @PathVariable workerId: Int
    ): ResponseEntity<DetailedShiftDto> {
        val actor = currentUserService.requireUser()
        val shift = shiftService.removeWorkerFromShift(workerId, shiftId, actor)
        return ResponseEntity.status(HttpStatus.OK).body(shift)
    }
}
