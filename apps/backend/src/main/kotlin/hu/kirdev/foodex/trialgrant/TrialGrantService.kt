package hu.kirdev.foodex.trialgrant

import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TrialGrantService(
    private val trialGrantRepository: TrialGrantRepository,
    private val userRepository: UserRepository,
) {

    @Transactional(readOnly = true)
    fun getAllGrants(): List<TrialGrantDto> {
        return trialGrantRepository.findAll()
            .sortedBy { it.name.lowercase() }
            .map { TrialGrantDto(it) }
    }

    @Transactional(readOnly = true)
    fun existsByInternalId(internalId: String): Boolean {
        return trialGrantRepository.existsByInternalId(internalId)
    }

    @Transactional(readOnly = false)
    fun createGrant(request: CreateTrialGrantDto): TrialGrantDto {
        val name = request.name.trim()
        val internalId = request.internalId.trim()
        validateFields(name, internalId)
        if (trialGrantRepository.existsByInternalId(internalId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A trial grant already exists for this internalId")
        }

        val saved = trialGrantRepository.save(
            TrialGrantEntity(
                name = name,
                internalId = internalId,
            )
        )
        applyGrant(internalId)
        return TrialGrantDto(saved)
    }

    @Transactional(readOnly = false)
    fun updateGrant(id: Int, request: UpdateTrialGrantDto): TrialGrantDto {
        val grant = trialGrantRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Trial grant not found") }

        val name = request.name.trim()
        val internalId = request.internalId.trim()
        validateFields(name, internalId)

        val previousInternalId = grant.internalId
        if (internalId != previousInternalId && trialGrantRepository.existsByInternalId(internalId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A trial grant already exists for this internalId")
        }

        grant.name = name
        grant.internalId = internalId
        val saved = trialGrantRepository.save(grant)

        if (internalId != previousInternalId) {
            revokeGrant(previousInternalId)
            applyGrant(internalId)
        }

        return TrialGrantDto(saved)
    }

    @Transactional(readOnly = false)
    fun deleteGrant(id: Int) {
        val grant = trialGrantRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Trial grant not found") }
        val internalId = grant.internalId
        trialGrantRepository.delete(grant)
        revokeGrant(internalId)
    }

    private fun validateFields(name: String, internalId: String) {
        if (name.isEmpty() || internalId.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and internalId are required")
        }
    }

    private fun applyGrant(internalId: String) {
        val user = userRepository.findUserEntityByInternalId(internalId) ?: return
        if (user.role == Role.GUEST) {
            user.role = Role.TRIAL
            user.isActive = true
            userRepository.save(user)
        }
    }

    private fun revokeGrant(internalId: String) {
        val user = userRepository.findUserEntityByInternalId(internalId) ?: return
        if (user.role == Role.TRIAL) {
            user.role = Role.GUEST
            user.isActive = false
            userRepository.save(user)
        }
    }
}
