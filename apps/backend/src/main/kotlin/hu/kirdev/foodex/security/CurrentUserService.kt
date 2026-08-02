package hu.kirdev.foodex.security

import hu.kirdev.foodex.oidcuser.FoodExOidcUser
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

/**
 * Resolves the authenticated FoodEx user from the security context
 */
@Service
class CurrentUserService(
    private val userRepository: UserRepository,
) {

    @Transactional(readOnly = true)
    fun requireUser(): UserEntity {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated")

        val principal = authentication.principal
        val internalId = when (principal) {
            is FoodExOidcUser -> principal.internalId
            else -> throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unsupported principal")
        }

        return userRepository.findUserEntityByInternalId(internalId)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not registered")
    }
}
