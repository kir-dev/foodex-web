package hu.kirdev.foodex.model

import hu.kirdev.foodex.converter.IntListConverter
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

    @Convert(converter = IntListConverter::class)
    @Column(columnDefinition = "TEXT")
    var members: List<Int> = emptyList(),

    @Convert(converter = IntListConverter::class)
    @Column(columnDefinition = "TEXT")
    var newbies: List<Int> = emptyList(),

    @Column(length = 255)
    var comment: String = ""
)
