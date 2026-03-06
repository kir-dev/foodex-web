package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.OpeningRequestEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OpeningRequestRepository : JpaRepository<OpeningRequestEntity, Int> {
    fun findAllByCookingClubId(cookingClubId: Int): List<OpeningRequestEntity>
    fun findAllByUserId(userId: Int): List<OpeningRequestEntity>
    fun findAllByIsAcceptedTrue(): List<OpeningRequestEntity>
    fun findAllByIsAcceptedFalse(): List<OpeningRequestEntity>
}