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
    fun getAllActiveUsers(): List<UserEntity> {
        return userRepository.findUserEntitiesByIsActiveTrue()
    }

    @Transactional(readOnly = true)
    fun getAllUnactiveUsers(): List<UserEntity> {
        return userRepository.findUserEntitiesByIsActiveFalse()
    }

    @Transactional(readOnly = false)
    fun createUser(
        name: String,
        email: String,
        role: Role = Role.GUEST,
        nickname: String = name,
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
}