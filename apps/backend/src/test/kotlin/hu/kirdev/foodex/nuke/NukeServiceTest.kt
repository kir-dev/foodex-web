package hu.kirdev.foodex.nuke

import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.shift.ShiftRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NukeServiceTest {

    private lateinit var shiftRepository: ShiftRepository
    private lateinit var openingRequestRepository: OpeningRequestRepository
    private lateinit var service: NukeService

    @BeforeEach
    fun setUp() {
        shiftRepository = mockk()
        openingRequestRepository = mockk()
        service = NukeService(shiftRepository, openingRequestRepository)
    }

    @Test
    fun `deleteAllOpeningRequestsAndShifts deletes shifts then opening requests`() {
        every { shiftRepository.count() } returns 3
        every { openingRequestRepository.count() } returns 2
        every { shiftRepository.deleteAll() } returns Unit
        every { openingRequestRepository.deleteAll() } returns Unit

        service.deleteAllOpeningRequestsAndShifts()

        verifyOrder {
            shiftRepository.deleteAll()
            openingRequestRepository.deleteAll()
        }
    }
}
