package hu.kirdev.foodex.service

import hu.kirdev.foodex.model.CookingClubEntity
import hu.kirdev.foodex.model.UserEntity
import hu.kirdev.foodex.repository.CookingClubRepository
import hu.kirdev.foodex.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CookingClubService(
    private val cookingClubRepository: CookingClubRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getAllCookingClubs() : List<CookingClubEntity> {
        return cookingClubRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getCookingClub(cookingClubId: Long) : CookingClubEntity? {
        return cookingClubRepository.findCookingClubEntityById(cookingClubId)
    }

//    @Transactional(readOnly = true)
//    fun getLeadersByClubId(cookingClubId: Long) : List<UserEntity> {
//
//    }
//    fun createCookingClub(club: CookingClubEntity) {}
//    fun deleteCookingClub(clubId: Long) {}
//    fun updateCookingClub(clubId: Long, club: CookingClubEntity) {}
//    fun addLeaderToCookingClub(clubId: Long, leaderId: Long) {}
//    fun removeLeaderToCookingClub(clubId: Long) {}

}