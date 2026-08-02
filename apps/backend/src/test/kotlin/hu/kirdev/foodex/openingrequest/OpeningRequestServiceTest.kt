package hu.kirdev.foodex.openingrequest

import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.cookingclub.CookingClubService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

class OpeningRequestServiceTest {

    private lateinit var repository: OpeningRequestRepository
    private lateinit var cookingClubService: CookingClubService
    private lateinit var service: OpeningRequestService

    private val club = CookingClubEntity(id = 403, name = "Americano")
    private val opening = LocalDateTime.now().plusDays(1)
    private val closing = opening.plusHours(2)

    @BeforeEach
    fun setUp() {
        repository = mockk()
        cookingClubService = mockk()
        service = OpeningRequestService(repository, cookingClubService)
        every { cookingClubService.getCookingClubEntity(403) } returns club
    }

    @Test
    fun `createOpeningRequest succeeds for club leader`() {
        val leader = user(1, Role.MEMBER)
        every { cookingClubService.isLeaderOfCookingClub(1, 403) } returns true
        every { repository.save(any()) } answers {
            OpeningRequestEntity(
                id = 10,
                user = leader,
                cookingClub = club,
                opening = opening,
                closing = closing,
                place = "10. konyha",
                description = "desc",
            )
        }

        val dto = service.createOpeningRequest(
            CreateOpeningRequestDto(
                cookingClubId = 403,
                opening = opening,
                closing = closing,
                place = "10. konyha",
                description = "desc",
            ),
            actor = leader,
        )

        assertEquals(10, dto.id)
        assertEquals(1, dto.user.id)
        verify(exactly = 1) { repository.save(any()) }
    }

    @Test
    fun `createOpeningRequest forbidden for non-leader non-admin`() {
        val guest = user(2, Role.MEMBER)
        every { cookingClubService.isLeaderOfCookingClub(2, 403) } returns false

        val ex = assertThrows<ResponseStatusException> {
            service.createOpeningRequest(
                CreateOpeningRequestDto(
                    cookingClubId = 403,
                    opening = opening,
                    closing = closing,
                    place = "10. konyha",
                    description = "desc",
                ),
                actor = guest,
            )
        }
        assertEquals(HttpStatus.FORBIDDEN, ex.statusCode)
    }

    @Test
    fun `createOpeningRequest allowed for admin without being leader`() {
        val admin = user(3, Role.ADMIN)
        every { repository.save(any()) } answers {
            OpeningRequestEntity(
                id = 11,
                user = admin,
                cookingClub = club,
                opening = opening,
                closing = closing,
                place = "x",
                description = "y",
            )
        }

        val dto = service.createOpeningRequest(
            CreateOpeningRequestDto(
                cookingClubId = 403,
                opening = opening,
                closing = closing,
                place = "x",
                description = "y",
            ),
            actor = admin,
        )
        assertEquals(11, dto.id)
    }

    @Test
    fun `createOpeningRequest rejects inverted times`() {
        val admin = user(3, Role.ADMIN)
        val ex = assertThrows<ResponseStatusException> {
            service.createOpeningRequest(
                CreateOpeningRequestDto(
                    cookingClubId = 403,
                    opening = closing,
                    closing = opening,
                    place = "x",
                    description = "y",
                ),
                actor = admin,
            )
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    private fun user(id: Int, role: Role) = UserEntity(
        id = id,
        internalId = "internal-$id",
        role = role,
        name = "User $id",
        nickname = null,
        email = "u$id@test.com",
        favouriteQuote = null,
        isActive = true,
    )
}
