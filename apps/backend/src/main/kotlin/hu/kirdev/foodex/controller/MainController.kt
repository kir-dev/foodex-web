package hu.kirdev.foodex.controller

import hu.kirdev.foodex.dto.ApplyForShiftDTO
import hu.kirdev.foodex.dto.CreateShiftFromRequestDTO
import hu.kirdev.foodex.dto.FoodExRequestDTO
import hu.kirdev.foodex.dto.HomePageResponseDTO
import hu.kirdev.foodex.dto.OrdersResponseDTO
import hu.kirdev.foodex.dto.ProfilesResponseDTO
import hu.kirdev.foodex.dto.ShiftsResponseDTO
import hu.kirdev.foodex.model.FoodExRequestEntity
import hu.kirdev.foodex.model.ShiftEntity
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
    /********** HOME ***********************************************************************************/
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

    /********** REQUEST ********************************************************************************/
    @PostMapping("/request")
    fun createFoodExRequest(@Valid @RequestBody request: FoodExRequestDTO) : FoodExRequestEntity {
        return foodExRequestService.createFoodExRequest(request)
    }

    /********** INCOMING-REQUESTS **********************************************************************/
    @GetMapping("/incoming-requests")
    fun getOrders() : OrdersResponseDTO {
        return OrdersResponseDTO(
            incomingFoodExRequests = foodExRequestService.getFoodExRequestsByIsAcceptedFalse(),
            acceptedShifts = shiftService.getActiveShifts()
        )
    }

    @PostMapping("/incoming-requests")
    fun createShiftsFromRequest(@Valid @RequestBody request: CreateShiftFromRequestDTO) : List<ShiftEntity> {
        return shiftService.createShiftFromFoodExRequest(request)
    }

    /********** OPENINGS *******************************************************************************/
    @GetMapping("/openings")
    fun getOpenings() : List<ShiftEntity> {
        return shiftService.getAllShiftsInSemester()
    }

    @PutMapping("/openings")
    fun updateShift(@Valid @RequestBody shift: ShiftEntity) : ShiftEntity {
        return shiftService.updateShift(shift)
    }

    /********** SHIFTS *********************************************************************************/
    @GetMapping("/shifts")
    fun getShifts() : ShiftsResponseDTO {
        return ShiftsResponseDTO(
            activeShifts = shiftService.getActiveShifts(),
            fullShifts = shiftService.getFullShifts()
        )
    }

    @PostMapping("/shifts/member")
    fun addMemberToShift(@Valid @RequestBody application: ApplyForShiftDTO) : ShiftEntity {
        return shiftService.addMemberToShift(
            userId = application.userId,
            shiftId = application.shiftId
        )
    }

    @PostMapping("/shifts/newbie")
    fun addNewbieToShift(@Valid @RequestBody application: ApplyForShiftDTO) : ShiftEntity {
        return shiftService.addNewbieToShift(
            userId = application.userId,
            shiftId = application.shiftId
        )
    }

    @DeleteMapping("/shifts/member")
    fun removeMemberFromShift(@Valid @RequestBody application: ApplyForShiftDTO) : ShiftEntity {
        return shiftService.removeMemberFromShift(
            userId = application.userId,
            shiftId = application.shiftId
        )
    }

    @DeleteMapping("/shifts/newbie")
    fun removeNewbieFromShift(@Valid @RequestBody application: ApplyForShiftDTO) : ShiftEntity {
        return shiftService.removeNewbieFromShift(
            userId = application.userId,
            shiftId = application.shiftId
        )
    }

    /********** USER ***********************************************************************************/
    @GetMapping("/user/{userId}")
    fun getUser(@PathVariable userId: Int) : ProfilesResponseDTO {
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

    @PutMapping("/user")
    fun updateUser(@Valid @RequestBody user: UserEntity) : UserEntity {
        return userService.updateUser(user)
    }
}