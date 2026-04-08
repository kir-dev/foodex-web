package hu.kirdev.foodex.config

import hu.kirdev.foodex.openingrequest.OpeningRequestDto
import hu.kirdev.foodex.user.UserDto
import java.time.LocalDateTime

data class ConfigurationDto (
    val feelingOfTheWeek: String,
    val foodExLogo: String,
    val startOfSemester: LocalDateTime,
    val endOfSemester: LocalDateTime,
) {
    constructor(config: ConfigurationEntity) : this(
        feelingOfTheWeek = config.feelingOfTheWeek,
        foodExLogo = config.foodExLogo,
        startOfSemester = config.startOfSemester,
        endOfSemester = config.endOfSemester,
    )
}

data class UpdateConfigurationDto (
    val feelingOfTheWeek: String,
    val foodExLogo: String,
    val startOfSemester: LocalDateTime,
    val endOfSemester: LocalDateTime,
)

data class HomePageDto (
    val feelingOfTheWeek: String,
    val foodExLogo: String,
    val activeMembers: List<UserDto>,
    val upcomingOpenings: List<OpeningRequestDto>,
)