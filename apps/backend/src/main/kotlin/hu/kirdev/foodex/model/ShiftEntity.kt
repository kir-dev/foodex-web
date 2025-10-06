package hu.kirdev.foodex.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "shifts")
data class ShiftEntity(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column
    val cookingClubId: Int,

    @Column
    var maxMembers: Int = 20,

    @Column
    var opening: LocalDateTime,

    @Column
    var closing: LocalDateTime,

    @Column(length = 30)
    var place: String,

    @ElementCollection
    @CollectionTable(
        name = "shift_members",
        joinColumns = [JoinColumn(name = "shift_id")]
    )
    @Column(name = "member_id")
    var members: List<Int> = emptyList(),

    @ElementCollection
    @CollectionTable(
        name = "shift_newbies",
        joinColumns = [JoinColumn(name = "shift_id")]
    )
    @Column(name = "newbie_id")
    var newbies: List<Int> = emptyList(),

    @Column(length = 255)
    var comment: String = ""
)
