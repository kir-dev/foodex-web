package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.CookingClubEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CookingClubRepository : JpaRepository<CookingClubEntity, Int> {
    fun findCookingClubEntityById(id: Int): CookingClubEntity?

    @Query("SELECT c FROM CookingClubEntity c WHERE :leaderId IN (c.leaders)")
    fun findAllByLeaderId(leaderId: Int): List<CookingClubEntity>

    @Query("SELECT c.leaders FROM CookingClubEntity c WHERE c.id = :clubId")
    fun findLeadersByCookingClubId(cookingClubId: Int): List<Int>
}