package hu.kirdev.foodex.service

import hu.kirdev.foodex.model.ConfigurationEntity
import hu.kirdev.foodex.repository.ShiftRepository
import hu.kirdev.foodex.repository.UserRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ShiftServiceTest {
    val shiftRepository = mockk<ShiftRepository>()
    val userRepository = mockk<UserRepository>()
    val configurationService = mockk<ConfigurationService>()
    val foodExService = mockk<FoodExRequestService>()

    @Test
    fun getAllShiftsInSemester() {


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