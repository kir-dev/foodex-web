package hu.kirdev.foodex.cookingclub

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
@RequestMapping("/api/cooking-clubs")
class CookingClubController(private val cookingClubService: CookingClubService) {

    @Operation(summary = "Get cooking clubs")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Cooking clubs found",
            content = [Content(schema = Schema(implementation = DetailedCookingClubDto::class))]
        )
    )
    @GetMapping()
    fun getCookingClubs() : ResponseEntity<List<DetailedCookingClubDto>> {
        val clubs = cookingClubService.getAllCookingClubs()
        return ResponseEntity.status(HttpStatus.OK).body(clubs)
    }

    @Operation(summary = "Get a cooking club")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Cooking club found",
                content = [Content(schema = Schema(implementation = DetailedCookingClubDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Cooking club not found")
        ]
    )
    @GetMapping("/{clubId}")
    fun getCookingClub(@PathVariable clubId: Int) : ResponseEntity<DetailedCookingClubDto> {
        val club = cookingClubService.getCookingClub(clubId)
        return ResponseEntity.status(HttpStatus.OK).body(club)
    }

    @Operation(summary = "Create a cooking club")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Cooking club created",
            content = [Content(schema = Schema(implementation = DetailedCookingClubDto::class))]
        )
    )
    @PostMapping()
    fun createCookingClub(@RequestBody createRequest: CreateCookingClubDto) : ResponseEntity<DetailedCookingClubDto> {
        val club = cookingClubService.createCookingClub(createRequest)
        return ResponseEntity.status(HttpStatus.OK).body(club)
    }

    @Operation(summary = "Update a cooking club")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Cooking club updated",
                content = [Content(schema = Schema(implementation = DetailedCookingClubDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Cooking club not found")
        ]
    )
    @PostMapping("/{clubId}")
    fun updateCookingClub(
        @PathVariable clubId: Int,
        @RequestBody updateRequest: UpdateCookingClubDto
        ) : ResponseEntity<DetailedCookingClubDto> {

        val club = cookingClubService.updateCookingClub(clubId, updateRequest)
        return ResponseEntity.status(HttpStatus.OK).body(club)
    }

    @Operation(summary = "Delete a cooking club")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Cooking club deleted",
            ),
            ApiResponse(responseCode = "404", description = "Cooking club not found")
        ]
    )
    @DeleteMapping("/{clubId}")
    fun updateCookingClub(@PathVariable clubId: Int) : ResponseEntity<Void> {
        cookingClubService.deleteCookingClub(clubId)
        return ResponseEntity.noContent().build()
    }
}