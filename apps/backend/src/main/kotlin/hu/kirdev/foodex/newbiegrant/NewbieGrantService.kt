package hu.kirdev.foodex.newbiegrant

import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class NewbieGrantService(
    private val newbieGrantRepository: NewbieGrantRepository,
    private val userRepository: UserRepository,
) {

    @Transactional(readOnly = true)
    fun getAllGrants(): List<NewbieGrantDto> {
        return newbieGrantRepository.findAll()
            .sortedBy { it.name.lowercase() }
            .map { NewbieGrantDto(it) }
    }

    @Transactional(readOnly = true)
    fun existsByInternalId(internalId: String): Boolean {
        return newbieGrantRepository.existsByInternalId(internalId)
    }

    @Transactional(readOnly = false)
    fun createGrant(request: CreateNewbieGrantDto): NewbieGrantDto {
        val name = request.name.trim()
        val internalId = request.internalId.trim()
        validateFields(name, internalId)
        if (newbieGrantRepository.existsByInternalId(internalId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A newbie grant already exists for this internalId")
        }

        val saved = newbieGrantRepository.save(
            NewbieGrantEntity(
                name = name,
                internalId = internalId,
            )
        )
        applyGrant(internalId)
        return NewbieGrantDto(saved)
    }

    @Transactional(readOnly = false)
    fun updateGrant(id: Int, request: UpdateNewbieGrantDto): NewbieGrantDto {
        val grant = newbieGrantRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Newbie grant not found") }

        val name = request.name.trim()
        val internalId = request.internalId.trim()
        validateFields(name, internalId)

        val previousInternalId = grant.internalId
        if (internalId != previousInternalId && newbieGrantRepository.existsByInternalId(internalId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "A newbie grant already exists for this internalId")
        }

        grant.name = name
        grant.internalId = internalId
        val saved = newbieGrantRepository.save(grant)

        if (internalId != previousInternalId) {
            revokeGrant(previousInternalId)
            applyGrant(internalId)
        }

        return NewbieGrantDto(saved)
    }

    @Transactional(readOnly = false)
    fun deleteGrant(id: Int) {
        val grant = newbieGrantRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Newbie grant not found") }
        val internalId = grant.internalId
        newbieGrantRepository.delete(grant)
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
            user.role = Role.NEWBIE
            userRepository.save(user)
        }
    }

    private fun revokeGrant(internalId: String) {
        val user = userRepository.findUserEntityByInternalId(internalId) ?: return
        if (user.role == Role.NEWBIE) {
            user.role = Role.GUEST
            userRepository.save(user)
        }
    }
}
