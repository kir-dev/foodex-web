package hu.kirdev.foodex.config

import hu.kirdev.foodex.openingrequest.DetailedOpeningRequestDto
import hu.kirdev.foodex.user.UserDto
import java.time.LocalDateTime

data class ConfigurationDto (
    val feelingOfTheWeek: String,
    val foodExLogo: String,
    val homepageDescription: String,
    val startOfSemester: LocalDateTime,
    val endOfSemester: LocalDateTime,
) {
    constructor(config: ConfigurationEntity) : this(
        feelingOfTheWeek = config.feelingOfTheWeek,
        foodExLogo = config.foodExLogo,
        homepageDescription = config.homepageDescription,
        startOfSemester = config.startOfSemester,
        endOfSemester = config.endOfSemester,
    )
}

data class UpdateConfigurationDto (
    val feelingOfTheWeek: String?,
    val foodExLogo: String?,
    val homepageDescription: String?,
    val startOfSemester: LocalDateTime?,
    val endOfSemester: LocalDateTime?,
)

data class HomepageDto (
    val feelingOfTheWeek: String,
    val foodExLogo: String,
    val homepageDescription: String,
    val activeMembers: List<UserDto>,
    val upcomingOpenings: List<DetailedOpeningRequestDto>,  // TODO: ???
)