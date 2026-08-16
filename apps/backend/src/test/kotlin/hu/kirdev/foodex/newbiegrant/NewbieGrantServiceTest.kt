package hu.kirdev.foodex.newbiegrant

import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class NewbieGrantServiceTest {

    private lateinit var grantRepository: NewbieGrantRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: NewbieGrantService

    @BeforeEach
    fun setUp() {
        grantRepository = mockk()
        userRepository = mockk()
        service = NewbieGrantService(grantRepository, userRepository)
    }

    @Test
    fun `createGrant stores entry and upgrades existing guest`() {
        val guest = user(2, Role.GUEST, "guest-id")
        every { grantRepository.existsByInternalId("guest-id") } returns false
        every { grantRepository.save(any()) } answers {
            (invocation.args[0] as NewbieGrantEntity).copy(id = 1)
        }
        every { userRepository.findUserEntityByInternalId("guest-id") } returns guest
        every { userRepository.save(guest) } returns guest

        val dto = service.createGrant(CreateNewbieGrantDto(name = "  Teszt Próbás  ", internalId = "  guest-id  "))

        assertEquals(1, dto.id)
        assertEquals("Teszt Próbás", dto.name)
        assertEquals("guest-id", dto.internalId)
        assertEquals(Role.NEWBIE, guest.role)
        verify(exactly = 1) { userRepository.save(guest) }
    }

    @Test
    fun `createGrant does not override member or admin`() {
        val member = user(3, Role.MEMBER, "member-id")
        every { grantRepository.existsByInternalId("member-id") } returns false
        every { grantRepository.save(any()) } answers { (invocation.args[0] as NewbieGrantEntity).copy(id = 2) }
        every { userRepository.findUserEntityByInternalId("member-id") } returns member

        service.createGrant(CreateNewbieGrantDto(name = "Tag", internalId = "member-id"))

        assertEquals(Role.MEMBER, member.role)
        verify(exactly = 0) { userRepository.save(member) }
    }

    @Test
    fun `createGrant rejects duplicate internalId`() {
        every { grantRepository.existsByInternalId("dup") } returns true

        val ex = assertThrows<ResponseStatusException> {
            service.createGrant(CreateNewbieGrantDto(name = "A", internalId = "dup"))
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun `createGrant rejects blank fields`() {
        val ex = assertThrows<ResponseStatusException> {
            service.createGrant(CreateNewbieGrantDto(name = "   ", internalId = "id"))
        }
        assertEquals(HttpStatus.BAD_REQUEST, ex.statusCode)
    }

    @Test
    fun `updateGrant can change name only`() {
        val grant = NewbieGrantEntity(id = 4, name = "Régi", internalId = "same-id")
        every { grantRepository.findById(4) } returns Optional.of(grant)
        every { grantRepository.save(grant) } returns grant

        val dto = service.updateGrant(4, UpdateNewbieGrantDto(name = "Új név", internalId = "same-id"))

        assertEquals("Új név", dto.name)
        assertEquals("same-id", dto.internalId)
        verify(exactly = 0) { userRepository.findUserEntityByInternalId(any()) }
    }

    @Test
    fun `updateGrant revokes old and grants new internalId`() {
        val grant = NewbieGrantEntity(id = 5, name = "Próbás", internalId = "old-id")
        val previous = user(1, Role.NEWBIE, "old-id")
        val next = user(2, Role.GUEST, "new-id")
        every { grantRepository.findById(5) } returns Optional.of(grant)
        every { grantRepository.existsByInternalId("new-id") } returns false
        every { grantRepository.save(grant) } returns grant
        every { userRepository.findUserEntityByInternalId("old-id") } returns previous
        every { userRepository.findUserEntityByInternalId("new-id") } returns next
        every { userRepository.save(previous) } returns previous
        every { userRepository.save(next) } returns next

        service.updateGrant(5, UpdateNewbieGrantDto(name = "Próbás", internalId = "new-id"))

        assertEquals(Role.GUEST, previous.role)
        assertEquals(Role.NEWBIE, next.role)
    }

    @Test
    fun `deleteGrant downgrades newbie but not member`() {
        val grant = NewbieGrantEntity(id = 6, name = "Próbás", internalId = "nid")
        val newbie = user(8, Role.NEWBIE, "nid")
        every { grantRepository.findById(6) } returns Optional.of(grant)
        every { grantRepository.delete(grant) } returns Unit
        every { userRepository.findUserEntityByInternalId("nid") } returns newbie
        every { userRepository.save(newbie) } returns newbie

        service.deleteGrant(6)

        assertEquals(Role.GUEST, newbie.role)
        verify(exactly = 1) { userRepository.save(newbie) }
    }

    @Test
    fun `deleteGrant does not downgrade member`() {
        val grant = NewbieGrantEntity(id = 7, name = "Tag", internalId = "mid")
        val member = user(9, Role.MEMBER, "mid")
        every { grantRepository.findById(7) } returns Optional.of(grant)
        every { grantRepository.delete(grant) } returns Unit
        every { userRepository.findUserEntityByInternalId("mid") } returns member

        service.deleteGrant(7)

        assertEquals(Role.MEMBER, member.role)
        verify(exactly = 0) { userRepository.save(member) }
    }

    @Test
    fun `deleteGrant throws when missing`() {
        every { grantRepository.findById(99) } returns Optional.empty()
        val ex = assertThrows<ResponseStatusException> {
            service.deleteGrant(99)
        }
        assertEquals(HttpStatus.NOT_FOUND, ex.statusCode)
    }

    private fun user(id: Int, role: Role, internalId: String) = UserEntity(
        id = id,
        internalId = internalId,
        role = role,
        name = "User $id",
        nickname = null,
        email = "u$id@test.com",
        favouriteQuote = null,
        isActive = true,
    )
}
