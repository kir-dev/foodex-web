package hu.kirdev.foodex.user

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { UserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUserById(id: Int) : UserDto {
        return userRepository.findById(id)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
            .let { UserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUserByInternalId(internalId: String) : UserDto? {
        return userRepository.findUserEntityByInternalId(internalId)?.let { UserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUserByNameOrNickname(nameOrNickname: String): UserDto? {
        return userRepository.findUserEntityByNameOrNicknameIgnoreCase(nameOrNickname, nameOrNickname)
            ?.let { UserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getUsersByNameOrNickname(nameOrNickname: String): List<UserDto> {
        return userRepository.findUserEntitiesByNameOrNicknameIgnoreCase(nameOrNickname, nameOrNickname)
            .map { UserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getActiveUsers(): List<UserDto> {
        return userRepository.findUserEntitiesByIsActiveTrue().map { UserDto(it) }
    }

    @Transactional(readOnly = true)
    fun getInactiveUsers(): List<UserDto> {
        return userRepository.findUserEntitiesByIsActiveFalse().map { UserDto(it) }
    }

    @Transactional(readOnly = false)
    fun updateUser(id: Int, updateTo: UpdateUserDto): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        // Update non-null fields
        updateTo.name?.let { user.name = it }
        updateTo.nickname?.let { user.nickname = it }
        updateTo.email?.let { user.email = it }
        updateTo.favouriteQuote?.let { user.favouriteQuote = it }
        updateTo.profilePicture?.let { user.profilePicture = it }

        return UserDto(userRepository.save(user))
    }

    @Transactional(readOnly = false)
    fun deleteUser(id: Int) {
        userRepository.findById(id).orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        userRepository.deleteById(id)
    }

    @Transactional(readOnly = false)
    fun updateRole(userId: Int, role: Role): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        user.role = role

        return UserDto(userRepository.save(user))
    }

    @Transactional(readOnly = false)
    fun updateActivation(userId: Int, isActive: Boolean): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        user.isActive = isActive

        return UserDto(userRepository.save(user))
    }
}