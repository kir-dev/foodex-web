package hu.kirdev.foodex.service

import hu.kirdev.foodex.model.ConfigurationEntity
import hu.kirdev.foodex.model.ShiftEntity
import hu.kirdev.foodex.model.UserEntity
import hu.kirdev.foodex.repository.ShiftRepository
import hu.kirdev.foodex.repository.UserRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

class ShiftServiceTest {
    val shiftRepository = mockk<ShiftRepository>()
    val userRepository = mockk<UserRepository>()
    val configurationService = mockk<ConfigurationService>()
    val foodExService = mockk<FoodExRequestService>()

    @Test
    fun getAllShiftsInSemester() {
        /*

        every { shiftRepository.findAll() } returns listOf(

        )

        every {shiftRepository.findAllByUserId(any())} returns listOf(

        )


        val service = ShiftService(
            shiftRepository,
            userRepository,
            configurationService,
            foodExService
        )

        val result = service.getAllShiftsInSemester()
        assertEquals(1, result.size)
        assertEquals("asdf", result.first().comment)
        verify(exactly = 1) { userRepository.findAll() }

         */
    }

    @Test
    fun addMemberToShift() {
        every { shiftRepository.findAll()} returns listOf(
            ShiftEntity(
                id = 1,
                cookingClubId = 1,
                maxMembers = 10,
                opening = LocalDateTime.now().plusHours(1),
                closing = LocalDateTime.now().plusHours(2),
                place = "-1 nagykonyha",
                members = listOf(1),
                newbies = emptyList(),
                comment = "eredeti shift"
            )
        )

        val shiftService = ShiftService(
            shiftRepository,
            userRepository,
            configurationService,
            foodExService
        )

        assertEquals(1L, shiftRepository.count())

        shiftService.addMemberToShift(1, 2)

        val result = shiftRepository.count()
        assertEquals(2L, result)
    }

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { configurationService.get() } returns ConfigurationEntity(
            0,
            "asdfsdf",
            "asdfa",
            LocalDateTime.now(),
            LocalDateTime.now()
        )
    }

}