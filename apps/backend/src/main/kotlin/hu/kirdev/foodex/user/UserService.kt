package hu.kirdev.foodex.user

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(private val userRepository: UserRepository) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getAllUsers(): List<DetailedUserDto> {
        return userRepository.findAll().map { DetailedUserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUserById(id: Int): DetailedUserDto {
        return userRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
            .let { DetailedUserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUserByInternalId(internalId: String): UserEntity? {
        return userRepository.findUserEntityByInternalId(internalId)
    }

    @Transactional(readOnly = true)
    fun getUserByNameOrNickname(nameOrNickname: String): DetailedUserDto? {
        return userRepository.findUserEntityByNameOrNicknameIgnoreCase(nameOrNickname, nameOrNickname)
            ?.let { DetailedUserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUsersByNameOrNickname(nameOrNickname: String): List<DetailedUserDto> {
        return userRepository.findUserEntitiesByNameOrNicknameIgnoreCase(nameOrNickname, nameOrNickname)
            .map { DetailedUserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getActiveUsers(): List<DetailedUserDto> {
        return userRepository.findUserEntitiesByIsActiveTrue().map { DetailedUserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getInactiveUsers(): List<DetailedUserDto> {
        return userRepository.findUserEntitiesByIsActiveFalse().map { DetailedUserDto(it) }
    }

    // Self or ADMIN
    @Transactional(readOnly = false)
    fun updateUser(id: Int, updateTo: UpdateUserDto, actor: UserEntity): DetailedUserDto {
        if (!actor.role.isAdminOrAbove() && actor.id != id) {
            log.warn("Rejected profile update: actorId={} targetUserId={}", actor.id, id)
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Can only update own profile")
        }

        val user = userRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        updateTo.name?.let { user.name = it }
        updateTo.nickname?.let {
            if (it.length > 10) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "nickname must be at most 10 characters")
            }
            user.nickname = it
        }
        updateTo.email?.let { user.email = it }
        updateTo.favouriteQuote?.let { user.favouriteQuote = it }
        updateTo.profilePicture?.let { user.profilePicture = it }

        val saved = userRepository.save(user)
        log.info("Updated user id={} actorId={}", saved.id, actor.id)
        return DetailedUserDto(saved)
    }

    // Internal (OIDC / system)
    @Transactional(readOnly = false)
    fun updateUser(user: UserEntity): UserEntity {
        return userRepository.save(user)
    }

    @Transactional(readOnly = false)
    fun deleteUser(id: Int) {
        userRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        userRepository.deleteById(id)
        log.info("Deleted user id={}", id)
    }

    @Transactional(readOnly = false)
    fun updateRole(userId: Int, role: Role): DetailedUserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val previous = user.role
        user.role = role
        val saved = userRepository.save(user)
        log.info("Updated user id={} role {} -> {}", saved.id, previous, saved.role)
        return DetailedUserDto(saved)
    }

    @Transactional(readOnly = false)
    fun activateUser(userId: Int): DetailedUserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        user.isActive = true
        val saved = userRepository.save(user)
        log.info("Activated user id={}", saved.id)
        return DetailedUserDto(saved)
    }

    @Transactional(readOnly = false)
    fun deActivateUser(userId: Int): DetailedUserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        user.isActive = false
        val saved = userRepository.save(user)
        log.info("Deactivated user id={}", saved.id)
        return DetailedUserDto(saved)
    }
}
