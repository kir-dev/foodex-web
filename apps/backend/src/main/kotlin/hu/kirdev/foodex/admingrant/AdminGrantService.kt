package hu.kirdev.foodex.admingrant

import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AdminGrantService(
    private val adminGrantRepository: AdminGrantRepository,
    private val userRepository: UserRepository,
) {

    @Transactional(readOnly = true)
    fun getAllGrants(): List<AdminGrantDto> {
        return adminGrantRepository.findAll()
            .sortedBy { it.name.lowercase() }
            .map { AdminGrantDto(it) }
    }

    @Transactional(readOnly = true)
    fun existsByInternalId(internalId: String): Boolean {
        return adminGrantRepository.existsByInternalId(internalId)
    }

    @Transactional(readOnly = false)
    fun createGrant(request: CreateAdminGrantDto): AdminGrantDto {
        val name = request.name.trim()
        val internalId = request.internalId.trim()
        validateFields(name, internalId)
        if (adminGrantRepository.existsByInternalId(internalId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "An admin grant already exists for this internalId")
        }

        val saved = adminGrantRepository.save(
            AdminGrantEntity(
                name = name,
                internalId = internalId,
            )
        )
        applyGrant(internalId)
        return AdminGrantDto(saved)
    }

    @Transactional(readOnly = false)
    fun updateGrant(id: Int, request: UpdateAdminGrantDto): AdminGrantDto {
        val grant = adminGrantRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Admin grant not found") }

        val name = request.name.trim()
        val internalId = request.internalId.trim()
        validateFields(name, internalId)

        val previousInternalId = grant.internalId
        if (internalId != previousInternalId && adminGrantRepository.existsByInternalId(internalId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "An admin grant already exists for this internalId")
        }

        grant.name = name
        grant.internalId = internalId
        val saved = adminGrantRepository.save(grant)

        if (internalId != previousInternalId) {
            revokeGrant(previousInternalId)
            applyGrant(internalId)
        }

        return AdminGrantDto(saved)
    }

    @Transactional(readOnly = false)
    fun deleteGrant(id: Int) {
        val grant = adminGrantRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Admin grant not found") }
        val internalId = grant.internalId
        adminGrantRepository.delete(grant)
        revokeGrant(internalId)
    }

    private fun validateFields(name: String, internalId: String) {
        if (name.isEmpty() || internalId.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and internalId are required")
        }
    }

    private fun applyGrant(internalId: String) {
        val user = userRepository.findUserEntityByInternalId(internalId) ?: return
        if (user.role != Role.SUPERUSER) {
            user.role = Role.ADMIN
            user.isActive = true
            userRepository.save(user)
        }
    }

    private fun revokeGrant(internalId: String) {
        val user = userRepository.findUserEntityByInternalId(internalId) ?: return
        if (user.role == Role.ADMIN) {
            user.role = Role.GUEST
            user.isActive = false
            userRepository.save(user)
        }
    }
}
