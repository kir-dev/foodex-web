package hu.kirdev.foodex.service

import hu.kirdev.foodex.dto.FoodExRequestDTO
import hu.kirdev.foodex.model.FoodExRequestEntity
import hu.kirdev.foodex.repository.FoodExRequestRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import io.mockk.verify

class FoodExRequestServiceTest {
    private val foodExRequestRepository = mockk<FoodExRequestRepository>()
    private val foodExRequestService = FoodExRequestService(
        foodExRequestRepository,
        cookingClubService,
        configurationService
    )

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun createFoodExRequest() {
        // Arrange
        val foodExRequestDTO = FoodExRequestDTO(
            userId = 1,
            cookingClubId = 2,
            opening = LocalDateTime.of(2025, 10, 10, 10, 0),
            closing = LocalDateTime.of(2025, 10, 10, 12, 0),
            place = "Kitchen",
            description = "EZT ADTAM HOZZA"
        )

        val expectedEntity = FoodExRequestEntity(
            id = 1,
            isAccepted = false,
            userId = foodExRequestDTO.userId,
            cookingClubId = foodExRequestDTO.cookingClubId,
            opening = foodExRequestDTO.opening,
            closing = foodExRequestDTO.closing,
            place = foodExRequestDTO.place,
            description = foodExRequestDTO.description
        )

        every { foodExRequestRepository.save(any()) } returns expectedEntity

        // Act
        val result = foodExRequestService.createFoodExRequest(foodExRequestDTO)

        // Assert
        verify(exactly = 1) { foodExRequestRepository.save(any()) }
        assertEquals(expectedEntity, result)
        assertEquals(foodExRequestDTO.userId, result.userId)
        assertEquals(foodExRequestDTO.cookingClubId, result.cookingClubId)
        assertEquals(foodExRequestDTO.opening, result.opening)
        assertEquals(foodExRequestDTO.closing, result.closing)
        assertEquals(foodExRequestDTO.place, result.place)
        assertEquals(foodExRequestDTO.description, result.description)
        assertEquals(false, result.isAccepted)
        assertEquals(1, result.id)
    }


}