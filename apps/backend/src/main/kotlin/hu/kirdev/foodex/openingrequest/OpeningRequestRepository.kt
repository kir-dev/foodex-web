package hu.kirdev.foodex.openingrequest

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface OpeningRequestRepository : JpaRepository<OpeningRequestEntity, Int> {

    fun findAllByIsAcceptedTrue(): List<OpeningRequestEntity>
    fun findAllByIsAcceptedFalse(): List<OpeningRequestEntity>

    @Query(
        """
        SELECT r FROM OpeningRequestEntity r
        JOIN FETCH r.user
        JOIN FETCH r.cookingClub
        WHERE r.isAccepted = :accepted AND r.opening > :now
        ORDER BY r.opening
        """
    )
    fun findUpcomingByAccepted(
        @Param("accepted") accepted: Boolean,
        @Param("now") now: LocalDateTime,
    ): List<OpeningRequestEntity>

    @Query(
        """
        SELECT r FROM OpeningRequestEntity r
        JOIN FETCH r.user
        JOIN FETCH r.cookingClub
        WHERE r.isAccepted = :accepted AND r.closing > :now
        ORDER BY r.opening
        """
    )
    fun findCurrentOrUpcomingByAccepted(
        @Param("accepted") accepted: Boolean,
        @Param("now") now: LocalDateTime,
    ): List<OpeningRequestEntity>

    @Query(
        """
        SELECT r FROM OpeningRequestEntity r
        JOIN FETCH r.user
        JOIN FETCH r.cookingClub
        WHERE r.closing > :start AND r.opening < :end
        ORDER BY r.opening
        """
    )
    fun findOverlappingSemester(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
    ): List<OpeningRequestEntity>

    @Query(
        """
        SELECT r FROM OpeningRequestEntity r
        JOIN FETCH r.user
        JOIN FETCH r.cookingClub
        WHERE r.id = :id
        """
    )
    fun findByIdWithDetails(@Param("id") id: Int): OpeningRequestEntity?
}
