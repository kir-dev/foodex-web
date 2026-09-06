package hu.kirdev.foodex.cookingclub

import hu.kirdev.foodex.user.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CookingClubService(
    private val cookingClubRepository: CookingClubRepository,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(readOnly = true)
    fun getAllCookingClubs(): List<DetailedCookingClubDto> {
        return cookingClubRepository.findAll().map { DetailedCookingClubDto(it) }
    }

    @Transactional(readOnly = true)
    fun getCookingClub(id: Int): DetailedCookingClubDto {
        return cookingClubRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cooking club not found") }
            .let { DetailedCookingClubDto(it) }
    }

    @Transactional(readOnly = true)
    fun getCookingClubEntity(id: Int): CookingClubEntity {
        return cookingClubRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cooking club not found") }
    }

    @Transactional(readOnly = false)
    fun createCookingClub(club: CreateCookingClubDto): DetailedCookingClubDto {
        val saved = cookingClubRepository.save(
            CookingClubEntity(
                id = club.id,
                name = club.name,
            )
        )
        log.info("Created cooking club id={} name={}", saved.id, saved.name)
        return DetailedCookingClubDto(saved)
    }

    @Transactional(readOnly = false)
    fun updateCookingClub(id: Int, updateTo: UpdateCookingClubDto): DetailedCookingClubDto {
        val club = cookingClubRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cooking club not found") }

        club.name = updateTo.name
        val saved = cookingClubRepository.save(club)
        log.info("Updated cooking club id={} name={}", saved.id, saved.name)
        return DetailedCookingClubDto(saved)
    }

    @Transactional(readOnly = false)
    fun updateCookingClub(club: CookingClubEntity): CookingClubEntity {
        return cookingClubRepository.save(club)
    }

    @Transactional(readOnly = false)
    fun deleteCookingClub(id: Int) {
        cookingClubRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cooking club not found") }

        cookingClubRepository.deleteById(id)
        log.info("Deleted cooking club id={}", id)
    }

    @Transactional(readOnly = false)
    fun addLeaderToCookingClub(leaderId: Int, cookingClubId: Int): DetailedCookingClubDto {
        val user = userRepository.findById(leaderId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val club = cookingClubRepository.findById(cookingClubId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found") }

        if (!club.leaders.any { it.id == user.id }) {
            club.leaders.add(user)
            log.info("Added leader userId={} to cooking club id={}", user.id, club.id)
        }

        return DetailedCookingClubDto(cookingClubRepository.save(club))
    }

    @Transactional(readOnly = false)
    fun removeLeaderFromCookingClub(leaderId: Int, cookingClubId: Int): DetailedCookingClubDto {
        val user = userRepository.findById(leaderId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val club = cookingClubRepository.findById(cookingClubId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Club not found") }

        club.leaders.remove(user)
        log.info("Removed leader userId={} from cooking club id={}", user.id, club.id)

        return DetailedCookingClubDto(cookingClubRepository.save(club))
    }

    @Transactional(readOnly = true)
    fun isLeaderOfCookingClub(userId: Int, cookingClubId: Int): Boolean {
        val club = cookingClubRepository.findById(cookingClubId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Cooking club not found") }

        return club.leaders.any { leader -> leader.id == userId }
    }
}
