package hu.kirdev.foodex.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "shifts")
data class ShiftEntity(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column
    val cookingClubId: Long,

    @Column
    var maxMembers: Int = 20,

    @Column
    var opening: LocalDateTime,

    @Column
    var closing: LocalDateTime,

    @Column(length = 30)
    var place: String,

    @Column
    var members: List<Long>,

    @Column
    var newbies: List<Long>,

    @Column(length = 255)
    var comment: String
)
