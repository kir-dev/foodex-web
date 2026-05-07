package hu.kirdev.foodex.openingrequest

import hu.kirdev.foodex.config.ConfigurationService
import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class OpeningRequestService(
    private val openingRequestRepository: OpeningRequestRepository,
    private val userRepository: UserRepository,
    private val cookingClubService: CookingClubService,
) {

    @Transactional(readOnly = true)
    fun getAllOpeningRequests() : List<DetailedOpeningRequestDto> {
        return openingRequestRepository.findAll().map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getOpeningRequestById(id: Int) : OpeningRequestDto {
        return openingRequestRepository.findById(id)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }
            .let { OpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUpcomingOpeningRequestsByIsAcceptedTrue() : List<DetailedOpeningRequestDto> {
        val now = LocalDateTime.now()
        return openingRequestRepository.findAllByIsAcceptedTrue()
            .filter { it.opening > now }
            .map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUpcomingOpeningRequestsByIsAcceptedFalse() : List<DetailedOpeningRequestDto> {
        val now = LocalDateTime.now()
        return openingRequestRepository.findAllByIsAcceptedFalse()
            .filter { it.opening > now }.map { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = false)
    fun createOpeningRequest(request: CreateOpeningRequestDto) : DetailedOpeningRequestDto {
        val user = userRepository.findById(request.userId)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val club = cookingClubService.getCookingClubEntity(request.cookingClubId)

        if (cookingClubService.isLeaderOfCookingClub(request.userId, request.cookingClubId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not leader of cooking club")
        }

        if (request.opening >= request.closing) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Closing must be after opening")
        }

        return openingRequestRepository.save(OpeningRequestEntity(
            user = user,
            cookingClub = club,
            opening = request.opening,
            closing = request.closing,
            place = request.place,
            description = request.description,
        )).let { DetailedOpeningRequestDto(it) }
    }

    @Transactional(readOnly = false)
    fun acceptOpeningRequest(id: Int) : DetailedOpeningRequestDto {
        val request = openingRequestRepository.findById(id)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        request.isAccepted = true

        return DetailedOpeningRequestDto(openingRequestRepository.save(request))
    }

    @Transactional(readOnly = false)
    fun deleteOpeningRequest(id: Int) {
        openingRequestRepository.deleteById(id)
    }

    @Transactional(readOnly = false)
    fun updateOpeningRequest(id: Int, toUpdate: UpdateOpeningRequestDto) : DetailedOpeningRequestDto {
        val request = openingRequestRepository.findById(id)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "Opening request not found") }

        toUpdate.opening?.let { request.opening = it }
        toUpdate.closing?.let { request.closing = it }
        toUpdate.place?.let { request.place = it }
        toUpdate.description?.let { request.description = it }

        return DetailedOpeningRequestDto(openingRequestRepository.save(request))
    }
}