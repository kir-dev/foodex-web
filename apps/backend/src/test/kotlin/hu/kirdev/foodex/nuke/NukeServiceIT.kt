package hu.kirdev.foodex.nuke

import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.openingrequest.OpeningRequestEntity
import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.shift.ShiftEntity
import hu.kirdev.foodex.shift.ShiftRepository
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
class NukeServiceIT {

    @Autowired
    private lateinit var nukeService: NukeService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var cookingClubRepository: CookingClubRepository

    @Autowired
    private lateinit var openingRequestRepository: OpeningRequestRepository

    @Autowired
    private lateinit var shiftRepository: ShiftRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    @Transactional
    fun `deletes all opening requests and shifts and keeps users and clubs`() {
        val user = userRepository.save(
            UserEntity(
                internalId = "nuke-it-user",
                role = Role.MEMBER,
                name = "Nuke User",
                nickname = "Nuke",
                email = "nuke@test.com",
                favouriteQuote = null,
                isActive = true,
            )
        )
        val club = cookingClubRepository.save(
            CookingClubEntity(id = 9001, name = "Nuke Club", leaders = mutableListOf(user))
        )
        val opening = LocalDateTime.now().plusDays(1)
        val closing = opening.plusHours(2)

        openingRequestRepository.save(
            OpeningRequestEntity(
                isAccepted = false,
                user = user,
                cookingClub = club,
                opening = opening,
                closing = closing,
                place = "kitchen",
                description = "unaccepted",
            )
        )
        val accepted = openingRequestRepository.save(
            OpeningRequestEntity(
                isAccepted = true,
                user = user,
                cookingClub = club,
                opening = opening.plusDays(1),
                closing = closing.plusDays(1),
                place = "kitchen",
                description = "accepted",
            )
        )
        shiftRepository.save(
            ShiftEntity(
                cookingClub = club,
                maxMembers = 3,
                opening = opening.plusDays(1),
                closing = closing.plusDays(1),
                place = "kitchen",
                comment = "shift",
                openingRequest = accepted,
                workers = mutableListOf(user),
            )
        )

        entityManager.flush()
        entityManager.clear()

        assertTrue(openingRequestRepository.count() >= 2)
        assertTrue(shiftRepository.count() >= 1)

        nukeService.deleteAllOpeningRequestsAndShifts()

        entityManager.flush()
        entityManager.clear()

        assertEquals(0, openingRequestRepository.count())
        assertEquals(0, shiftRepository.count())
        assertTrue(userRepository.count() >= 1)
        assertTrue(cookingClubRepository.existsById(9001))
    }
}
