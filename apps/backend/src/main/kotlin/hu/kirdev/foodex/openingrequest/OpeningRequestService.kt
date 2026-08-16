package hu.kirdev.foodex.openingrequest

import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.shift.ShiftRepository
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class OpeningRequestService(
    private val openingRequestRepository: OpeningRequestRepository,
    private val cookingClubService: CookingClubService,
    private val shiftRepository: ShiftRepository,
) {

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
            .findUpcomingByAccepted(accepted = true, now = LocalDateTime.now())
            .map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUpcomingOpeningRequestsByIsAcceptedFalse(): List<DetailedOpeningRequestDto> {
        return openingRequestRepository
            .findUpcomingByAccepted(accepted = false, now = LocalDateTime.now())
            .map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getCurrentOrUpcomingAcceptedOpeningRequests(): List<DetailedOpeningRequestDto> {
        return openingRequestRepository
            .findCurrentOrUpcomingByAccepted(accepted = true, now = LocalDateTime.now())
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

        val allowed = actor.role == Role.ADMIN ||
            cookingClubService.isLeaderOfCookingClub(actor.id, request.cookingClubId)
        if (!allowed) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not leader of cooking club")
        }

        if (!request.opening.isBefore(request.closing)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing must be after opening")
        }

        return openingRequestRepository.save(
            OpeningRequestEntity(
                user = actor,
                cookingClub = club,
                opening = request.opening,
                closing = request.closing,
                place = request.place,
                description = request.description,
            )
        ).let { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = false)
    fun acceptOpeningRequest(id: Int, actor: UserEntity): DetailedOpeningRequestDto {
        val request = openingRequestRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        requireOwnerLeaderOrAdmin(actor, request)

        request.isAccepted = true
        return DetailedOpeningRequestDto(openingRequestRepository.save(request))
    }

    @Transactional(readOnly = false)
    fun deleteOpeningRequest(id: Int, actor: UserEntity) {
        val request = openingRequestRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        requireOwnerLeaderOrAdmin(actor, request)
        val childShifts = shiftRepository.findAllByOpeningRequestId(id)
        if (childShifts.isNotEmpty()) {
            shiftRepository.deleteAll(childShifts)
        }
        openingRequestRepository.delete(request)
    }

    @Transactional(readOnly = false)
    fun updateOpeningRequest(id: Int, toUpdate: UpdateOpeningRequestDto, actor: UserEntity): DetailedOpeningRequestDto {
        val request = openingRequestRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        requireOwnerLeaderOrAdmin(actor, request)

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

        return DetailedOpeningRequestDto(openingRequestRepository.save(request))
    }

    private fun requireOwnerLeaderOrAdmin(actor: UserEntity, request: OpeningRequestEntity) {
        if (actor.role == Role.ADMIN) return
        if (actor.id == request.user.id) return
        if (cookingClubService.isLeaderOfCookingClub(actor.id, request.cookingClub.id)) return
        throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to modify this opening request")
    }
}
