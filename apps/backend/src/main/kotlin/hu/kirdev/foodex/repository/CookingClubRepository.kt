package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.CookingClubEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CookingClubRepository : JpaRepository<CookingClubEntity, Long> {
    fun findCookingClubEntityById(id: Long): CookingClubEntity?

    @Query("SELECT c FROM CookingClubEntity c WHERE :leaderId IN (c.leaders)")
    fun findAllByLeaderId(leaderId: Long): List<CookingClubEntity>

    @Query("SELECT c.leaders FROM CookingClubEntity c WHERE c.id = :clubId")
    fun findLeadersByCookingClubId(cookingClubId: Long): List<Long>
}