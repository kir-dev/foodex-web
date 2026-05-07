package hu.kirdev.foodex.openingrequest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OpeningRequestRepository : JpaRepository<OpeningRequestEntity, Int> {
    fun findAllByIsAcceptedTrue(): List<OpeningRequestEntity>
    fun findAllByIsAcceptedFalse(): List<OpeningRequestEntity>
}