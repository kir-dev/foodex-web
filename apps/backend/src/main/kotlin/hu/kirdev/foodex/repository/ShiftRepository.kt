package hu.kirdev.foodex.repository

import hu.kirdev.foodex.model.ShiftEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ShiftRepository : JpaRepository<ShiftEntity, Long> {
    fun findShiftEntityById(id: Long): ShiftEntity?
    fun findAllByCookingClubId(cookingClubId: Long): List<ShiftEntity>

    @Query("SELECT sh FROM ShiftEntity sh WHERE :userId IN (sh.members, sh.newbies)")
    fun findAllByUserId(userId: Long): List<ShiftEntity>

    @Query("SELECT sh FROM ShiftEntity sh WHERE (:userId IN (sh.members, sh.newbies)) AND sh.closing > CURRENT_TIMESTAMP")
    fun findActiveShiftsByUserId(userId: Long): List<ShiftEntity>

    @Query("SELECT sh from ShiftEntity sh WHERE sh.cookingClubId = :cookingClubId AND sh.closing > CURRENT_TIMESTAMP")
    fun findActiveShiftsByCookingClubId(cookingClubId: Long): List<ShiftEntity>

    @Query("SELECT sh FROM ShiftEntity sh WHERE sh.closing > CURRENT_TIMESTAMP")
    fun findActiveShifts(): List<ShiftEntity>
}