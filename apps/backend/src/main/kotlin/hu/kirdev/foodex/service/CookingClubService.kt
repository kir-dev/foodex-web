package hu.kirdev.foodex.service

import hu.kirdev.foodex.model.CookingClubEntity
import hu.kirdev.foodex.model.UserEntity
import hu.kirdev.foodex.repository.CookingClubRepository
import hu.kirdev.foodex.repository.ShiftRepository
import hu.kirdev.foodex.repository.UserRepository
import org.springframework.data.repository.findByIdOrNull
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
    fun getCookingClub(cookingClubId: Int) : CookingClubEntity? {
        return cookingClubRepository.findCookingClubEntityById(cookingClubId)
    }

    @Transactional(readOnly = true)
    fun getLeadersByClubId(cookingClubId: Int) : List<UserEntity> {
        val cookingClub = cookingClubRepository.findByIdOrNull(cookingClubId)
            ?: throw IllegalArgumentException("Cooking club with ID $cookingClubId not found")
        return userRepository.findAllById(cookingClub.leaders).toList()
    }

    @Transactional(readOnly = false)
    fun createCookingClub(name: String) : CookingClubEntity {
        if (name.length > 30) throw IllegalArgumentException("Name cannot be more than 30 characters")
        val cookingClub = CookingClubEntity(
            name = name,
            leaders = emptyList()
        )
        return cookingClubRepository.save(cookingClub)
    }

    fun deleteCookingClub(cookingClubId: Int) {
        val cookingClub = cookingClubRepository.findByIdOrNull(cookingClubId)
            ?: throw IllegalArgumentException("Cooking club with ID $cookingClubId not found")
        cookingClubRepository.delete(cookingClub)
    }

    fun updateCookingClub(cookingClub: CookingClubEntity) {
        cookingClubRepository.save(cookingClub)
    }

    fun addLeaderToCookingClub(cookingClubId: Int, leaderId: Int) : CookingClubEntity {
        val cookingClub = cookingClubRepository.findByIdOrNull(cookingClubId)
            ?: throw IllegalArgumentException("Cooking club with ID $cookingClubId not found")
        val leader = userRepository.findByIdOrNull(leaderId)
            ?: throw IllegalArgumentException("Leader with id $leaderId not found")

        if (cookingClub.leaders.contains(leaderId)) {
            throw IllegalArgumentException("Leader with id $leaderId is already a leader of the club")
        }

        val updatedLeaders = cookingClub.leaders.toMutableList().apply { add(leaderId) }
        cookingClub.leaders = updatedLeaders
        return cookingClubRepository.save(cookingClub)
    }

    fun removeLeaderToCookingClub(cookingClubId: Int, leaderId: Int) : CookingClubEntity {
        val cookingClub = cookingClubRepository.findByIdOrNull(cookingClubId)
            ?: throw IllegalArgumentException("Cooking club with ID $cookingClubId not found")
        val leader = userRepository.findByIdOrNull(leaderId)
            ?: throw IllegalArgumentException("Leader with id $leaderId not found")

        if (!cookingClub.leaders.contains(leaderId)) {
            throw IllegalArgumentException("Leader with id $leaderId is not a leader of the club")
        }

        val updatedLeaders = cookingClub.leaders.toMutableList().apply { remove(leaderId) }
        cookingClub.leaders = updatedLeaders
        return cookingClubRepository.save(cookingClub)
    }

}