package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.CookingClubEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CookingClubRepository : JpaRepository<CookingClubEntity, Int> {
    @Query("SELECT c FROM CookingClubEntity c WHERE :leaderId MEMBER OF c.leaders")
    fun findAllByLeaderIdInLeaders(leaderId: Int): List<CookingClubEntity>

    @Query("SELECT c.leaders FROM CookingClubEntity c WHERE c.id = :cookingClubId")
    fun findLeadersByCookingClubId(cookingClubId: Int): List<Int>
}