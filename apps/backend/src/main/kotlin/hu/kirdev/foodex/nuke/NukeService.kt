package hu.kirdev.foodex.nuke

import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.shift.ShiftRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NukeService(
    private val shiftRepository: ShiftRepository,
    private val openingRequestRepository: OpeningRequestRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = false)
    fun deleteAllOpeningRequestsAndShifts() {
        val shiftCount = shiftRepository.count()
        val requestCount = openingRequestRepository.count()
        log.warn("Nuking all opening requests and shifts: shifts={} requests={}", shiftCount, requestCount)
        shiftRepository.deleteAll()
        openingRequestRepository.deleteAll()
        log.warn("Nuke complete")
    }
}
