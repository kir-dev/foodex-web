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
    val id: Int,

    @ManyToOne
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

    @ManyToMany(mappedBy = "shifts")
    var members: MutableList<UserEntity>,

    @ManyToMany(mappedBy = "shifts")
    var newbies: MutableList<UserEntity>,
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