package hu.kirdev.foodex.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "foodEx_requests")
data class FoodEx_RequestEntity(
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column
    var isAccepted: Boolean = false,

    @Column
    val userId: Long,

    @Column
    val cookingClubId: Long,

    @Column
    val opening: LocalDateTime,

    @Column
    val closing: LocalDateTime,

    @Column(length = 30)
    val place: String,

    @Column(length = 255)
    val description: String
)
