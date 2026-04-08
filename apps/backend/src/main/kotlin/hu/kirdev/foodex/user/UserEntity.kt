package hu.kirdev.foodex.user

import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.openingrequest.OpeningRequestEntity
import hu.kirdev.foodex.shift.ShiftEntity
import jakarta.persistence.*

// Roles within FoodEx
enum class Role{
    ADMIN, MEMBER, NEWBIE, GUEST
}

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(unique = true, nullable = false, length = 36)
    val internalId: String,

    @Enumerated(EnumType.STRING) // Store enum as string in the database
    @Column(columnDefinition = "varchar(32)", nullable = false)
    var role: Role = Role.GUEST,

    @Column(nullable = false)
    var name: String,

    @Column
    var nickname: String?,

    @Column(nullable = false)
    var email: String,

    @Column(length = 255)
    var favouriteQuote: String?,

    @Column(nullable = false)
    var isActive: Boolean = false,

    @Column(columnDefinition = "text")
    var profilePicture: String? = null, // URL to picture

    @ManyToMany(mappedBy = "users")
    var leaderAt: MutableList<CookingClubEntity> = mutableListOf(),

    @ManyToMany(mappedBy = "users")
    var shifts: MutableList<ShiftEntity> = mutableListOf(),

    @OneToMany(mappedBy = "users")
    var requests: MutableList<OpeningRequestEntity> = mutableListOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserEntity) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return this::class.simpleName + "(id = $id)"
    }
}
