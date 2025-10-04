package hu.kirdev.foodex.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "configuration")
data class ConfigurationEntity (
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var feelingOfTheWeek: String,

    var foodExLogo: String,

    var startOfSemester: LocalDateTime,

    var endOfSemester: LocalDateTime
)