package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.ShiftEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ShiftRepository : JpaRepository<ShiftEntity, Int> {
    fun findShiftEntityById(id: Int): ShiftEntity?
    fun findAllByCookingClubId(cookingClubId: Int): List<ShiftEntity>

    @Query("SELECT sh FROM ShiftEntity sh WHERE :userId IN (sh.members, sh.newbies)")
    fun findAllByUserId(userId: Int): List<ShiftEntity>

    @Query("SELECT sh FROM ShiftEntity sh WHERE (:userId IN (sh.members, sh.newbies)) AND sh.closing > CURRENT_TIMESTAMP")
    fun findUpcomingShiftsByUserId(userId: Int): List<ShiftEntity>

    @Query("SELECT sh from ShiftEntity sh WHERE sh.cookingClubId = :cookingClubId AND sh.closing > CURRENT_TIMESTAMP")
    fun findUpcomingShiftsByCookingClubId(cookingClubId: Int): List<ShiftEntity>
}