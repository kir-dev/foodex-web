package hu.kirdev.foodex.cookingclub

import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class CookingClubServiceTest {

    private lateinit var cookingClubRepository: CookingClubRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: CookingClubService

    @BeforeEach
    fun setUp() {
        cookingClubRepository = mockk()
        userRepository = mockk()
        service = CookingClubService(cookingClubRepository, userRepository)
    }

    @Test
    fun `isLeaderOfCookingClub returns true when user is in leaders`() {
        val leader = user(id = 5)
        val club = CookingClubEntity(id = 403, name = "Americano", leaders = mutableListOf(leader))
        every { cookingClubRepository.findById(403) } returns Optional.of(club)

        assertTrue(service.isLeaderOfCookingClub(5, 403))
    }

    @Test
    fun `isLeaderOfCookingClub returns false when user is not leader`() {
        val leader = user(id = 5)
        val club = CookingClubEntity(id = 403, name = "Americano", leaders = mutableListOf(leader))
        every { cookingClubRepository.findById(403) } returns Optional.of(club)

        assertFalse(service.isLeaderOfCookingClub(403, 403))
        assertFalse(service.isLeaderOfCookingClub(99, 403))
    }

    @Test
    fun `isLeaderOfCookingClub throws when club missing`() {
        every { cookingClubRepository.findById(1) } returns Optional.empty()
        assertThrows<ResponseStatusException> {
            service.isLeaderOfCookingClub(1, 1)
        }
    }

    private fun user(id: Int) = UserEntity(
        id = id,
        internalId = "internal-$id",
        role = Role.MEMBER,
        name = "User $id",
        nickname = null,
        email = "u$id@test.com",
        favouriteQuote = null,
        isActive = true,
    )
}
