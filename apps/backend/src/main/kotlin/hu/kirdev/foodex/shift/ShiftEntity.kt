package hu.kirdev.foodex.shift

import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "shifts")
data class ShiftEntity(

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shft_cooking_club", nullable = false)
    var cookingClub: CookingClubEntity,

    @Column(nullable = false)
    var maxMembers: Int,

    @Column(nullable = false)
    var opening: LocalDateTime,

    @Column(nullable = false)
    var closing: LocalDateTime,

    @Column(nullable = false)
    var place: String,

    @Column(nullable = false)
    var comment: String = "",

    @ManyToMany
    @JoinTable(
        name = "shift_workers",
        joinColumns = [JoinColumn(name = "shift_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var workers: MutableList<UserEntity> = mutableListOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShiftEntity) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return this::class.simpleName + "(id = $id)"
    }
}