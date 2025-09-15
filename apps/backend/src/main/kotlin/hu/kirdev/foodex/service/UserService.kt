package hu.kirdev.foodex.service

import hu.kirdev.foodex.model.Role
import hu.kirdev.foodex.model.UserEntity
import hu.kirdev.foodex.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(private val userRepository: UserRepository) {

    @Transactional(readOnly = true)
    fun getAllUsers(): List<UserEntity> {
        return userRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserEntity? {
        return userRepository.findByIdOrNull(id)
    }

    @Transactional(readOnly = true)
    fun searchUserByNameOrNickname(nameOrNickname: String): UserEntity? {
        return userRepository.findUserEntityByNameOrNickname(nameOrNickname, nameOrNickname)
    }

    @Transactional(readOnly = true)
    fun searchUsersByNameOrNickname(nameOrNickname: String): List<UserEntity> {
        return userRepository.findUserEntitiesByNameOrNickname(nameOrNickname, nameOrNickname)
    }

    @Transactional(readOnly = true)
    fun getActiveUsers(): List<UserEntity> {
        return userRepository.findUserEntitiesByIsActiveTrue()
    }

    @Transactional(readOnly = true)
    fun getInactiveUsers(): List<UserEntity> {
        return userRepository.findUserEntitiesByIsActiveFalse()
    }

    @Transactional(readOnly = false)
    fun createUser(
        name: String,
        email: String,
        role: Role = Role.GUEST,
        nickname: String,
        isActive: Boolean = false
        ): UserEntity {

        val user = UserEntity(
            name = name,
            email = email,
            role = role,
            nickname = nickname,
            isActive = isActive
        )
        return userRepository.save(user)
    }

    @Transactional(readOnly = false)
    fun updateUser(user: UserEntity) : UserEntity {
        return userRepository.save(user)
    }

    @Transactional(readOnly = false)
    fun deleteUser(userId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw RuntimeException("User with id $userId not found")
        userRepository.delete(user)
    }

    @Transactional(readOnly = false)
    fun updateRole(userId: Long, role: Role): UserEntity {
        val user = userRepository.findByIdOrNull(userId)
        requireNotNull(user) { "User with id $userId not found" }
        user.role = role
        return userRepository.save(user)
    }

    @Transactional(readOnly = false)
    fun activateUser(userId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw RuntimeException("User with id $userId not found")
        user.isActive = true
        userRepository.save(user)
    }

    @Transactional(readOnly = false)
    fun deactivateUser(userId: Long) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw RuntimeException("User with id $userId not found")
        user.isActive = false
        userRepository.save(user)
    }
}