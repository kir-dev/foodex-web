package hu.kirdev.foodex.shift

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ShiftRepository : JpaRepository<ShiftEntity, Int> {
}