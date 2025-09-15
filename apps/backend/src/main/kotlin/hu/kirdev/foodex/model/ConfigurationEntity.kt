package hu.kirdev.foodex.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
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