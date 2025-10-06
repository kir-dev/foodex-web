package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.ShiftEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ShiftRepository : JpaRepository<ShiftEntity, Int> {
    fun findAllByCookingClubId(cookingClubId: Int): List<ShiftEntity>

    @Query("SELECT sh FROM ShiftEntity sh WHERE :userId MEMBER OF sh.members OR :userId MEMBER OF sh.newbies")
    fun findAllByUserIdInMembersOrNewbies(userId: Int): List<ShiftEntity>
}