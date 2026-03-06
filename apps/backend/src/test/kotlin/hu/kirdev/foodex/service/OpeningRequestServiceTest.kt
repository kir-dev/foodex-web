package hu.kirdev.foodex.service

import hu.kirdev.foodex.dto.OpeningRequestDTO
import hu.kirdev.foodex.model.OpeningRequestEntity
import hu.kirdev.foodex.repository.OpeningRequestRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import io.mockk.verify

class OpeningRequestServiceTest {
    private val openingRequestRepository = mockk<OpeningRequestRepository>()
    private val openingRequestService = OpeningRequestService(
        openingRequestRepository,
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
        val openingRequestDTO = OpeningRequestDTO(
            userId = 1,
            cookingClubId = 2,
            opening = LocalDateTime.of(2025, 10, 10, 10, 0),
            closing = LocalDateTime.of(2025, 10, 10, 12, 0),
            place = "Kitchen",
            description = "EZT ADTAM HOZZA"
        )

        val expectedEntity = OpeningRequestEntity(
            id = 1,
            isAccepted = false,
            userId = openingRequestDTO.userId,
            cookingClubId = openingRequestDTO.cookingClubId,
            opening = openingRequestDTO.opening,
            closing = openingRequestDTO.closing,
            place = openingRequestDTO.place,
            description = openingRequestDTO.description
        )

        every { openingRequestRepository.save(any()) } returns expectedEntity

        // Act
        val result = openingRequestService.createFoodExRequest(openingRequestDTO)

        // Assert
        verify(exactly = 1) { openingRequestRepository.save(any()) }
        assertEquals(expectedEntity, result)
        assertEquals(openingRequestDTO.userId, result.userId)
        assertEquals(openingRequestDTO.cookingClubId, result.cookingClubId)
        assertEquals(openingRequestDTO.opening, result.opening)
        assertEquals(openingRequestDTO.closing, result.closing)
        assertEquals(openingRequestDTO.place, result.place)
        assertEquals(openingRequestDTO.description, result.description)
        assertEquals(false, result.isAccepted)
        assertEquals(1, result.id)
    }


}