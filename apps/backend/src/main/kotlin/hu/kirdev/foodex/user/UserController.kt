package hu.kirdev.foodex.user

import hu.kirdev.foodex.security.CurrentUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val currentUserService: CurrentUserService,
) {

    @Operation(summary = "Get users")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Users found",
            content = [Content(schema = Schema(implementation = DetailedUserDto::class))]
        )
    )
    @GetMapping
    fun getUsers(): ResponseEntity<List<DetailedUserDto>> {
        val users = userService.getActiveUsers()
        return ResponseEntity.status(HttpStatus.OK).body(users)
    }

    @Operation(summary = "Search users by name or nickname")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Users found",
            content = [Content(schema = Schema(implementation = DetailedUserDto::class))]
        )
    )
    @GetMapping("/search")
    fun searchUsersByNameOrNickname(
        @RequestParam("q") nameOrNickname: String
    ): ResponseEntity<List<DetailedUserDto>> {
        val users = userService.getUsersByNameOrNickname(nameOrNickname)
        return ResponseEntity.status(HttpStatus.OK).body(users)
    }

    @Operation(summary = "Get the currently authenticated user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Current user",
                content = [Content(schema = Schema(implementation = DetailedUserDto::class))]
            ),
            ApiResponse(responseCode = "401", description = "Not authenticated"),
        ]
    )
    @GetMapping("/me")
    fun getCurrentUser(): ResponseEntity<DetailedUserDto> {
        val actor = currentUserService.requireUser()
        val user = userService.getUserById(actor.id)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

    @Operation(summary = "Get a user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User found",
                content = [Content(schema = Schema(implementation = DetailedUserDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "User not found"),
        ]
    )
    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: Int): ResponseEntity<DetailedUserDto> {
        val user = userService.getUserById(userId)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }

    @Operation(summary = "Update the user")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User updated",
                content = [Content(schema = Schema(implementation = DetailedUserDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "User not found"),
        ]
    )
    @PatchMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: Int,
        @Valid @RequestBody toUpdate: UpdateUserDto
    ): ResponseEntity<DetailedUserDto> {
        val actor = currentUserService.requireUser()
        val user = userService.updateUser(userId, toUpdate, actor)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }
}
