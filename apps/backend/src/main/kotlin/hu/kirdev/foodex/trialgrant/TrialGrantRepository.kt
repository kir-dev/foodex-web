package hu.kirdev.foodex.trialgrant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TrialGrantRepository : JpaRepository<TrialGrantEntity, Int> {
    fun findByInternalId(internalId: String): TrialGrantEntity?
    fun existsByInternalId(internalId: String): Boolean
}
