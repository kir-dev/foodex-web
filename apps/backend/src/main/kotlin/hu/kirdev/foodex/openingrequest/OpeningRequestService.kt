package hu.kirdev.foodex.openingrequest

import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.shift.ShiftRepository
import hu.kirdev.foodex.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Clock
import java.time.LocalDateTime

@Service
class OpeningRequestService(
    private val openingRequestRepository: OpeningRequestRepository,
    private val cookingClubService: CookingClubService,
    private val shiftRepository: ShiftRepository,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getAllOpeningRequests(): List<DetailedOpeningRequestDto> {
        return openingRequestRepository.findAll().map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getOpeningRequestById(id: Int): DetailedOpeningRequestDto {
        val request = openingRequestRepository.findByIdWithDetails(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found")
        return DetailedOpeningRequestDto(request)
    }

    @Transactional(readOnly = true)
    fun getOpeningRequestEntity(id: Int): OpeningRequestEntity {
        return openingRequestRepository.findByIdWithDetails(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found")
    }

    @Transactional(readOnly = true)
    fun getUpcomingOpeningRequestsByIsAcceptedTrue(): List<DetailedOpeningRequestDto> {
        return openingRequestRepository
            .findUpcomingByAccepted(accepted = true, now = now())
            .map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUpcomingOpeningRequestsByIsAcceptedFalse(): List<DetailedOpeningRequestDto> {
        return openingRequestRepository
            .findUpcomingByAccepted(accepted = false, now = now())
            .map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getCurrentOrUpcomingAcceptedOpeningRequests(): List<DetailedOpeningRequestDto> {
        return openingRequestRepository
            .findCurrentOrUpcomingByAccepted(accepted = true, now = now())
            .map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getOpeningRequestsInSemester(start: LocalDateTime, end: LocalDateTime): List<DetailedOpeningRequestDto> {
        return openingRequestRepository
            .findOverlappingSemester(start, end)
            .map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = false)
    fun createOpeningRequest(request: CreateOpeningRequestDto, actor: UserEntity): DetailedOpeningRequestDto {
        val club = cookingClubService.getCookingClubEntity(request.cookingClubId)

        val allowed = actor.role.isAdminOrAbove() ||
            cookingClubService.isLeaderOfCookingClub(actor.id, request.cookingClubId)
        if (!allowed) {
            log.warn("Rejected opening request create: actorId={} clubId={}", actor.id, request.cookingClubId)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not leader of cooking club")
        }

        if (!request.opening.isBefore(request.closing)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing must be after opening")
        }

        val saved = openingRequestRepository.save(
            OpeningRequestEntity(
                user = actor,
                cookingClub = club,
                opening = request.opening,
                closing = request.closing,
                place = request.place,
                description = request.description,
            )
        )
        log.info("Created opening request id={} clubId={} actorId={}", saved.id, club.id, actor.id)
        return DetailedOpeningRequestDto(saved)
    }

    @Transactional(readOnly = false)
    fun acceptOpeningRequest(id: Int, actor: UserEntity): DetailedOpeningRequestDto {
        val request = openingRequestRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        requireAdmin(actor)

        request.isAccepted = true
        val saved = openingRequestRepository.save(request)
        log.info("Accepted opening request id={} actorId={}", saved.id, actor.id)
        return DetailedOpeningRequestDto(saved)
    }

    @Transactional(readOnly = false)
    fun deleteOpeningRequest(id: Int, actor: UserEntity) {
        val request = openingRequestRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        requireAdmin(actor)
        val childShifts = shiftRepository.findAllByOpeningRequestId(id)
        if (childShifts.isNotEmpty()) {
            shiftRepository.deleteAll(childShifts)
        }
        openingRequestRepository.delete(request)
        log.info(
            "Deleted opening request id={} childShiftCount={} actorId={}",
            id,
            childShifts.size,
            actor.id,
        )
    }

    @Transactional(readOnly = false)
    fun updateOpeningRequest(id: Int, toUpdate: UpdateOpeningRequestDto, actor: UserEntity): DetailedOpeningRequestDto {
        val request = openingRequestRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        requireAdmin(actor)

        toUpdate.opening?.let { request.opening = it }
        toUpdate.closing?.let { request.closing = it }
        toUpdate.place?.let { newPlace ->
            request.place = newPlace
            shiftRepository.findAllByOpeningRequestId(id).forEach { shift ->
                shift.place = newPlace
            }
        }
        toUpdate.description?.let { request.description = it }

        if (!request.opening.isBefore(request.closing)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing must be after opening")
        }

        val saved = openingRequestRepository.save(request)
        log.info("Updated opening request id={} actorId={}", saved.id, actor.id)
        return DetailedOpeningRequestDto(saved)
    }

    private fun requireAdmin(actor: UserEntity) {
        if (!actor.role.isAdminOrAbove()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin required")
        }
    }

    private fun now(): LocalDateTime = LocalDateTime.now(clock)
}
