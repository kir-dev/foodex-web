package hu.kirdev.foodex.cookingclub

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CookingClubRepository : JpaRepository<CookingClubEntity, Int> {
}