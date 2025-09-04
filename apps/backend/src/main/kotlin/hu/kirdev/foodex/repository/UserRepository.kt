package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findUserEntityById(id: Long): UserEntity?
    fun findUserEntityByEmail(email: String): UserEntity?
    fun findUserEntityByName(name: String): UserEntity?
    fun findUserEntityByNickname(nickname: String): UserEntity?
    fun findUserEntityByNameOrNickname(name: String, nickname: String): UserEntity?
    fun findUserEntitiesByNameOrNickname(name: String, nickname: String): Collection<UserEntity>
    fun findUserEntitiesByIsActiveTrue(): List<UserEntity>
    fun findUserEntitiesByIsActiveFalse(): List<UserEntity>
}