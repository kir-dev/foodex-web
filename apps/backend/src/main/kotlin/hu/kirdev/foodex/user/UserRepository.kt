package hu.kirdev.foodex.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Int> {
    fun findUserEntityByInternalId(internalId: String): UserEntity?
    fun findUserEntityByNameOrNicknameIgnoreCase(name: String, nickname: String): UserEntity?
    fun findUserEntitiesByNameOrNicknameIgnoreCase(name: String, nickname: String): List<UserEntity>
    fun findUserEntitiesByIsActiveTrue(): List<UserEntity>
    fun findUserEntitiesByIsActiveFalse(): List<UserEntity>
}