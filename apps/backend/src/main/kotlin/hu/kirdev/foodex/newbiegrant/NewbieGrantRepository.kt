package hu.kirdev.foodex.newbiegrant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NewbieGrantRepository : JpaRepository<NewbieGrantEntity, Int> {
    fun findByInternalId(internalId: String): NewbieGrantEntity?
    fun existsByInternalId(internalId: String): Boolean
}
