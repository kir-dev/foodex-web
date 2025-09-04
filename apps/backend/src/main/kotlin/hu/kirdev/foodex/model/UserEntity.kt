package hu.kirdev.foodex.model

import jakarta.persistence.*
import jakarta.validation.constraints.*

// Roles withing FoodEx
enum class Role{
    ADMIN, MEMBER, NEWBIE, GUEST
}

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING) // Store enum as string in the database
    @Column(columnDefinition = "text", nullable = false)
    var role: Role = Role.GUEST,

    @Column(nullable = false)
    var name: String,

    @Column
    var nickname: String = name,

    @Column
    @Email
    var email: String,

    @Column(length = 255)
    var favouriteQuote: String = "",

    @Column
    var isActive: Boolean = false,

    @Column
    var profilePicture: String? = null // URL to picture
)
