package hu.kirdev.foodex.user

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
@RequestMapping("/api/user")
class UserController(private val userService: UserService) {

    @Operation(summary = "Get users")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Users found",
            content = [Content(schema = Schema(implementation = DetailedUserDto::class))]
        )
    )
    @GetMapping()
    fun getUsers() : ResponseEntity<List<DetailedUserDto>> {
        val users = userService.getActiveUsers()
        return ResponseEntity.status(HttpStatus.OK).body(users)
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
    fun getUser(@PathVariable userId: Int) : ResponseEntity<DetailedUserDto> {
        val user = userService.getUserById(userId)
        return ResponseEntity.status(HttpStatus.OK).body(user)
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
        ) : ResponseEntity<List<DetailedUserDto>> {

        val user = userService.getUsersByNameOrNickname(nameOrNickname)
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
    fun updateOpeningRequest(
        @PathVariable userId: Int,
        @RequestBody toUpdate: UpdateUserDto
    ) : ResponseEntity<DetailedUserDto>  {

        val user = userService.updateUser(userId, toUpdate)
        return ResponseEntity.status(HttpStatus.OK).body(user)
    }
}