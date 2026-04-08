package hu.kirdev.foodex.openingrequest

import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "requests")
data class OpeningRequestEntity(

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(nullable = false)
    var isAccepted: Boolean = false,

    @ManyToOne
    var user: UserEntity,

    @ManyToOne
    var cookingClub: CookingClubEntity,

    @Column(nullable = false)
    var opening: LocalDateTime,

    @Column(nullable = false)
    var closing: LocalDateTime,

    @Column(nullable = false)
    var place: String,

    @Column(nullable = false)
    var description: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OpeningRequestEntity) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return this::class.simpleName + "(id = $id)"
    }
}