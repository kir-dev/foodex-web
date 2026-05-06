package hu.kirdev.foodex.service

import hu.kirdev.foodex.dto.OpeningRequestDTO
import hu.kirdev.foodex.oidcuser.OpeningRequestEntity
import hu.kirdev.foodex.repository.OpeningRequestRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class OpeningRequestService(
    private val openingRequestRepository: OpeningRequestRepository,
    private val cookingClubService: CookingClubService,
    private val configurationService: ConfigurationService
) {

    @Transactional(readOnly = true)
    fun getAllOpeningRequests() : List<OpeningRequestEntity> {
        return openingRequestRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getOpeningRequestsById(id: Int) : OpeningRequestEntity? {
        return openingRequestRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun getOpeningRequestsByUserId(userId: Int) : List<OpeningRequestEntity> {
        val startOfSemester = configurationService.get().startOfSemester
        return openingRequestRepository.findAllByUserId(userId).filter { it.closing > startOfSemester }
    }

    @Transactional(readOnly = true)
    fun getOpeningRequestsByCookingClubId(cookingClubId: Int) : List<OpeningRequestEntity> {
        return openingRequestRepository.findAllByCookingClubId(cookingClubId)
    }

    @Transactional(readOnly = true)
    fun getOpeningRequestsByIsAcceptedTrue() : List<OpeningRequestEntity> {
        return openingRequestRepository.findAllByIsAcceptedTrue()
    }

    @Transactional(readOnly = true)
    fun getRelevantOpeningRequestsByIsAcceptedFalse() : List<OpeningRequestEntity> {
        val now = LocalDateTime.now()
        return openingRequestRepository.findAllByIsAcceptedFalse().filter { it.opening > now }
    }

    @Transactional(readOnly = false)
    fun createOpeningRequest(openingRequest: OpeningRequestDTO) : OpeningRequestEntity {

        if (!cookingClubService.isLeaderOfCookingClub(openingRequest.userId, openingRequest.cookingClubId)) {
            throw IllegalArgumentException("User with id ${openingRequest.userId} is NOT a LEADER of CookingClub with id ${openingRequest.cookingClubId}")
        }

        if (openingRequest.opening >= openingRequest.closing) {
            throw IllegalArgumentException("Closing must be after opening")
        }
        
        val request = OpeningRequestEntity(
            userId = openingRequest.userId,
            cookingClubId = openingRequest.cookingClubId,
            opening = openingRequest.opening,
            closing = openingRequest.closing,
            place = openingRequest.place,
            description = openingRequest.description
        )
        return openingRequestRepository.save(request)
    }

    @Transactional(readOnly = false)
    fun acceptOpeningRequest(id: Int) : OpeningRequestEntity? {
        return openingRequestRepository.findByIdOrNull(id)?.also {
            it.isAccepted = true
            openingRequestRepository.save(it)
        }
    }

    @Transactional(readOnly = false)
    fun deleteOpeningRequest(id: Int) {
        openingRequestRepository.deleteById(id)
    }
}