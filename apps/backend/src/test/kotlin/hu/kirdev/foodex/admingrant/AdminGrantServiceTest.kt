package hu.kirdev.foodex.admingrant

import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional

class AdminGrantServiceTest {

    private lateinit var grantRepository: AdminGrantRepository
    private lateinit var userRepository: UserRepository
    private lateinit var service: AdminGrantService

    @BeforeEach
    fun setUp() {
        grantRepository = mockk()
        userRepository = mockk()
        service = AdminGrantService(grantRepository, userRepository)
    }

    @Test
    fun `createGrant upgrades existing member to admin`() {
        val member = user(2, Role.MEMBER, "member-id")
        every { grantRepository.existsByInternalId("member-id") } returns false
        every { grantRepository.save(any()) } answers {
            (invocation.args[0] as AdminGrantEntity).copy(id = 1)
        }
        every { userRepository.findUserEntityByInternalId("member-id") } returns member
        every { userRepository.save(member) } returns member

        val dto = service.createGrant(CreateAdminGrantDto(name = "  Teszt Admin  ", internalId = "  member-id  "))

        assertEquals(1, dto.id)
        assertEquals("Teszt Admin", dto.name)
        assertEquals(Role.ADMIN, member.role)
        assertTrue(member.isActive)
        verify(exactly = 1) { userRepository.save(member) }
    }

    @Test
    fun `createGrant upgrades guest to admin`() {
        val guest = user(3, Role.GUEST, "guest-id")
        every { grantRepository.existsByInternalId("guest-id") } returns false
        every { grantRepository.save(any()) } answers { (invocation.args[0] as AdminGrantEntity).copy(id = 2) }
        every { userRepository.findUserEntityByInternalId("guest-id") } returns guest
        every { userRepository.save(guest) } returns guest

        service.createGrant(CreateAdminGrantDto(name = "Vendég", internalId = "guest-id"))

        assertEquals(Role.ADMIN, guest.role)
        assertTrue(guest.isActive)
    }

    @Test
    fun `createGrant does not override superuser`() {
        val superuser = user(4, Role.SUPERUSER, "su-id")
        every { grantRepository.existsByInternalId("su-id") } returns false
        every { grantRepository.save(any()) } answers { (invocation.args[0] as AdminGrantEntity).copy(id = 3) }
        every { userRepository.findUserEntityByInternalId("su-id") } returns superuser

        service.createGrant(CreateAdminGrantDto(name = "Su", internalId = "su-id"))

        assertEquals(Role.SUPERUSER, superuser.role)
        verify(exactly = 0) { userRepository.save(superuser) }
    }

    @Test
    fun `createGrant rejects duplicate internalId`() {
        every { grantRepository.existsByInternalId("dup") } returns true

        val ex = assertThrows<ResponseStatusException> {
            service.createGrant(CreateAdminGrantDto(name = "A", internalId = "dup"))
        }
        assertEquals(HttpStatus.CONFLICT, ex.statusCode)
    }

    @Test
    fun `deleteGrant downgrades admin to guest`() {
        val grant = AdminGrantEntity(id = 6, name = "Admin", internalId = "aid")
        val admin = user(8, Role.ADMIN, "aid")
        every { grantRepository.findById(6) } returns Optional.of(grant)
        every { grantRepository.delete(grant) } returns Unit
        every { userRepository.findUserEntityByInternalId("aid") } returns admin
        every { userRepository.save(admin) } returns admin

        service.deleteGrant(6)

        assertEquals(Role.GUEST, admin.role)
        assertFalse(admin.isActive)
        verify(exactly = 1) { userRepository.save(admin) }
    }

    @Test
    fun `deleteGrant does not downgrade superuser`() {
        val grant = AdminGrantEntity(id = 7, name = "Su", internalId = "sid")
        val superuser = user(9, Role.SUPERUSER, "sid")
        every { grantRepository.findById(7) } returns Optional.of(grant)
        every { grantRepository.delete(grant) } returns Unit
        every { userRepository.findUserEntityByInternalId("sid") } returns superuser

        service.deleteGrant(7)

        assertEquals(Role.SUPERUSER, superuser.role)
        verify(exactly = 0) { userRepository.save(superuser) }
    }

    @Test
    fun `updateGrant revokes old and grants new internalId`() {
        val grant = AdminGrantEntity(id = 5, name = "Admin", internalId = "old-id")
        val previous = user(1, Role.ADMIN, "old-id")
        val next = user(2, Role.MEMBER, "new-id")
        every { grantRepository.findById(5) } returns Optional.of(grant)
        every { grantRepository.existsByInternalId("new-id") } returns false
        every { grantRepository.save(grant) } returns grant
        every { userRepository.findUserEntityByInternalId("old-id") } returns previous
        every { userRepository.findUserEntityByInternalId("new-id") } returns next
        every { userRepository.save(previous) } returns previous
        every { userRepository.save(next) } returns next

        service.updateGrant(5, UpdateAdminGrantDto(name = "Admin", internalId = "new-id"))

        assertEquals(Role.GUEST, previous.role)
        assertEquals(Role.ADMIN, next.role)
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
