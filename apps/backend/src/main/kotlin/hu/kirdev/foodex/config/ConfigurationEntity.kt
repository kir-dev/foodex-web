package hu.kirdev.foodex.config

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "configuration")
data class ConfigurationEntity(

    @Id
    @Column(nullable = false)
    val id: Int = 1,

    @Column(nullable = false)
    var feelingOfTheWeek: String,

    @Column(nullable = false)
    var foodExLogo: String,

    @Column(nullable = false)
    var startOfSemester: LocalDateTime,

    @Column(nullable = false)
    var endOfSemester: LocalDateTime,
) {
    constructor(config: ConfigurationDto) : this(
        id = 1,
        feelingOfTheWeek = config.feelingOfTheWeek,
        foodExLogo = config.foodExLogo,
        startOfSemester = config.startOfSemester,
        endOfSemester = config.endOfSemester,
    )
}
