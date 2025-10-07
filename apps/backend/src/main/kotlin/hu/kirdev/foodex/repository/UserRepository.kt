package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Int> {
    fun findUserEntityByNameOrNicknameIgnoreCase(name: String, nickname: String): UserEntity?
    fun findUserEntitiesByNameOrNicknameIgnoreCase(name: String, nickname: String): List<UserEntity>
    fun findUserEntitiesByIsActiveTrue(): List<UserEntity>
    fun findUserEntitiesByIsActiveFalse(): List<UserEntity>
}