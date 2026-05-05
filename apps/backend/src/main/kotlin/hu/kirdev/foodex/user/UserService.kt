package hu.kirdev.foodex.user

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional(readOnly = true)
    fun getAllUsers(): List<DetailedUserDto> {
        return userRepository.findAll().map { DetailedUserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUserById(id: Int) : DetailedUserDto {
        return userRepository.findById(id)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
            .let { DetailedUserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUserByInternalId(internalId: String) : DetailedUserDto? {
        return userRepository.findUserEntityByInternalId(internalId)?.let { DetailedUserDto(it) }
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

    @Transactional(readOnly = false)
    fun updateUser(id: Int, updateTo: UpdateUserDto): DetailedUserDto {
        val user = userRepository.findById(id)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        // Update non-null fields
        updateTo.name?.let { user.name = it }
        updateTo.nickname?.let { user.nickname = it }
        updateTo.email?.let { user.email = it }
        updateTo.favouriteQuote?.let { user.favouriteQuote = it }
        updateTo.profilePicture?.let { user.profilePicture = it }

        return DetailedUserDto(userRepository.save(user))
    }

    @Transactional(readOnly = false)
    fun deleteUser(id: Int) {
        userRepository.findById(id).orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        userRepository.deleteById(id)
    }

    @Transactional(readOnly = false)
    fun updateRole(userId: Int, role: Role): DetailedUserDto {
        val user = userRepository.findById(userId)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        user.role = role

        return DetailedUserDto(userRepository.save(user))
    }

    @Transactional(readOnly = false)
    fun updateActivation(userId: Int, isActive: Boolean): DetailedUserDto {
        val user = userRepository.findById(userId)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        user.isActive = isActive

        return DetailedUserDto(userRepository.save(user))
    }
}