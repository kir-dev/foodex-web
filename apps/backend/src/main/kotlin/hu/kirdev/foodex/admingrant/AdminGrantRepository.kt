package hu.kirdev.foodex.admingrant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdminGrantRepository : JpaRepository<AdminGrantEntity, Int> {
    fun findByInternalId(internalId: String): AdminGrantEntity?
    fun existsByInternalId(internalId: String): Boolean
}
