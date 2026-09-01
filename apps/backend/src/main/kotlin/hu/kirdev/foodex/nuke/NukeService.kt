package hu.kirdev.foodex.nuke

import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.shift.ShiftRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NukeService(
    private val shiftRepository: ShiftRepository,
    private val openingRequestRepository: OpeningRequestRepository,
) {

    @Transactional(readOnly = false)
    fun deleteAllOpeningRequestsAndShifts() {
        shiftRepository.deleteAll()
        openingRequestRepository.deleteAll()
    }
}
