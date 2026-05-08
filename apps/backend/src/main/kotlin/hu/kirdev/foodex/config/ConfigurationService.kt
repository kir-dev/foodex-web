package hu.kirdev.foodex.config

import hu.kirdev.foodex.openingrequest.OpeningRequestService
import hu.kirdev.foodex.user.UserDto
import hu.kirdev.foodex.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ConfigurationService(
    private val configurationRepository: ConfigurationRepository,
    private val userRepository: UserRepository,
    private val openingRequestService: OpeningRequestService,
) {

    @Transactional(readOnly = false)
    fun get() : ConfigurationDto {
        val config = configurationRepository.findTopByOrderByIdDesc()

        if (config == null) {
            val configuration = ConfigurationEntity(
                id = 1,
                feelingOfTheWeek = "Feeling of the week :)",
                foodExLogo = "https://images.immediate.co.uk/production/volatile/sites/30/2020/08/chorizo-mozarella-gnocchi-bake-cropped-9ab73a3.jpg",
                startOfSemester = LocalDateTime.of(2026, 2, 1, 0, 0),
                endOfSemester = LocalDateTime.of(2026, 7, 8, 0, 0),
            )
            return ConfigurationDto(configurationRepository.save(configuration))
        }
        return ConfigurationDto(config)
    }

    @Transactional(readOnly = false)
    fun updateConfiguration(updateTo: UpdateConfigurationDto) : ConfigurationDto {
        val config = ConfigurationEntity(get())

        // Update non-null fields
        updateTo.feelingOfTheWeek?.let { config.feelingOfTheWeek = it }
        updateTo.foodExLogo?.let { config.foodExLogo = it }
        updateTo.startOfSemester?.let { config.startOfSemester = it }
        updateTo.endOfSemester?.let { config.endOfSemester = it }

        return ConfigurationDto(configurationRepository.save(config))
    }

    @Transactional(readOnly = true)
    fun getHomepage() : HomepageDto {
        val config = get()

        return HomepageDto(
            feelingOfTheWeek = config.feelingOfTheWeek,
            foodExLogo = config.foodExLogo,
            activeMembers = userRepository.findUserEntitiesByIsActiveTrue().map { UserDto(it) },    // TODO: only members???
            upcomingOpenings = openingRequestService.getUpcomingOpeningRequestsByIsAcceptedTrue()
        )
    }
}