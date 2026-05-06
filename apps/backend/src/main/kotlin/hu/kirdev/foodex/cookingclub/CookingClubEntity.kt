package hu.kirdev.foodex.cookingclub

import hu.kirdev.foodex.openingrequest.OpeningRequestEntity
import hu.kirdev.foodex.shift.ShiftEntity
import hu.kirdev.foodex.user.UserEntity
import jakarta.persistence.*
import jakarta.validation.constraints.Size

@Entity
@Table(name = "cooking_clubs")
data class CookingClubEntity(
    @Id
    @Column(nullable = false)
    val id: Int,

    @Column(length = 30, nullable = false)
    @field:Size(max = 30)
    var name: String,

    @ManyToMany
    @JoinTable(
        name = "user_cooking_club_leadership",
        joinColumns = [JoinColumn(name = "club_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var leaders: MutableList<UserEntity> = mutableListOf(),

    @ManyToMany(mappedBy = "cooking_clubs") // TODO: fix?
    var shifts: MutableList<ShiftEntity> = mutableListOf(),

    @OneToMany(mappedBy = "cooking_clubs")  // TODO: fix?
    var requests: MutableList<OpeningRequestEntity> = mutableListOf(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CookingClubEntity) return false
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String {
        return this::class.simpleName + "(id = $id)"
    }
}