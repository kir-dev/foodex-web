package hu.kirdev.foodex.controller

import hu.kirdev.foodex.dto.FoodExRequestDTO
import hu.kirdev.foodex.dto.HomePageResponseDTO
import hu.kirdev.foodex.dto.ProfilesResponseDTO
import hu.kirdev.foodex.dto.ShiftsResponseDTO
import hu.kirdev.foodex.model.FoodExRequestEntity
import hu.kirdev.foodex.model.UserEntity
import hu.kirdev.foodex.service.ConfigurationService
import hu.kirdev.foodex.service.FoodExRequestService
import hu.kirdev.foodex.service.ShiftService
import hu.kirdev.foodex.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class MainController(
    private val configurationService: ConfigurationService,
    private val shiftService: ShiftService,
    private val userService: UserService,
    private val foodExRequestService: FoodExRequestService
) {
    @GetMapping("/home")
    fun getHomePage() : HomePageResponseDTO {
        val config = configurationService.get()

        val homePage = HomePageResponseDTO(
            feelingOfTheWeek = config.feelingOfTheWeek,
            foodExLogo = config.foodExLogo,
            activeMembers = userService.getActiveUsers(),
            upcomingShifts = shiftService.getUpcomingShifts()
        )
        return homePage
    }

    @PostMapping("/request")
    fun createFoodExRequest(@Valid @RequestBody request: FoodExRequestDTO) : FoodExRequestEntity {
        return foodExRequestService.createFoodExRequest(request)
    }

    @GetMapping("/shifts")
    fun getShifts() : ShiftsResponseDTO {
        return ShiftsResponseDTO(
            activeShifts = shiftService.getActiveShifts(),
            fullShifts = shiftService.getFullShifts()
        )
    }

    //@PostMapping("/shifts")
    //fun applyForShift()
    // + jelentkezes leadas

    @GetMapping("/user/{userId}")
    fun getUser(@PathVariable userId: Long) : ProfilesResponseDTO {
        val user = userService.getUserById(userId)
            ?: throw RuntimeException("User not found")

        val data = ProfilesResponseDTO(
            userId = user.id,
            role = user.role,
            name = user.name,
            nickname = user.nickname,
            email = user.email,
            favouriteQuote = user.favouriteQuote,
            isActive = user.isActive,
            profilePicture = user.profilePicture,
            shifts = shiftService.getAllShiftsByUserId(user.id),
            requests = foodExRequestService.getFoodExRequestsByUserId(user.id)
        )
        return data
    }
    // + modify user

}