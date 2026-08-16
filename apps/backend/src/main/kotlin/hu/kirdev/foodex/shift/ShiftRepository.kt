package hu.kirdev.foodex.shift

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ShiftRepository : JpaRepository<ShiftEntity, Int> {

    /** Shifts overlapping [start, end) (semester window). */
    @Query(
        """
        SELECT s FROM ShiftEntity s
        JOIN FETCH s.cookingClub
        WHERE s.closing > :start AND s.opening < :end
        ORDER BY s.opening
        """
    )
    fun findOverlappingSemester(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
    ): List<ShiftEntity>

    /** Upcoming shifts (still open after now). */
    @Query(
        """
        SELECT s FROM ShiftEntity s
        JOIN FETCH s.cookingClub
        WHERE s.closing > :now
        ORDER BY s.opening
        """
    )
    fun findUpcomingWithClub(@Param("now") now: LocalDateTime): List<ShiftEntity>

    @Query(
        """
        SELECT s FROM ShiftEntity s
        JOIN FETCH s.cookingClub
        WHERE s.id = :id
        """
    )
    fun findByIdWithClub(@Param("id") id: Int): ShiftEntity?

    @Query(
        """
        SELECT s FROM ShiftEntity s
        JOIN FETCH s.cookingClub
        WHERE s.openingRequest.id = :requestId
        ORDER BY s.opening
        """
    )
    fun findAllByOpeningRequestIdWithClub(@Param("requestId") requestId: Int): List<ShiftEntity>

    fun findAllByOpeningRequestId(openingRequestId: Int): List<ShiftEntity>
}
