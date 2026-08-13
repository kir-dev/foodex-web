package hu.kirdev.foodex.config

import hu.kirdev.foodex.openingrequest.OpeningRequestService
import hu.kirdev.foodex.user.Role
import hu.kirdev.foodex.user.UserDto
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class ConfigurationService(
    private val configurationRepository: ConfigurationRepository,
    private val userRepository: UserRepository,
    private val openingRequestService: OpeningRequestService,
) {

    /**
     * READ with write-on-miss: returns config, seeding a default row if none exists
     * (keeps local/demo usable without a separate seed for config).
     */
    @Transactional(readOnly = false)
    fun get(): ConfigurationDto {
        val config = configurationRepository.findTopByOrderByIdDesc()

        if (config == null) {
            val configuration = ConfigurationEntity(
                id = 1,
                feelingOfTheWeek = "Feeling of the week :)",
                foodExLogo = "https://images.immediate.co.uk/production/volatile/sites/30/2020/08/chorizo-mozarella-gnocchi-bake-cropped-9ab73a3.jpg",
                startOfSemester = LocalDateTime.of(2026, 8, 1, 0, 0),
                endOfSemester = LocalDateTime.of(2027, 2, 1, 0, 0),
            )
            return ConfigurationDto(configurationRepository.save(configuration))
        }
        return ConfigurationDto(config)
    }

    /** WRITE — caller must be ADMIN */
    @Transactional(readOnly = false)
    fun updateConfiguration(updateTo: UpdateConfigurationDto, actor: UserEntity): ConfigurationDto {
        if (actor.role != Role.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only")
        }

        val config = ConfigurationEntity(get())

        updateTo.feelingOfTheWeek?.let { config.feelingOfTheWeek = it }
        updateTo.foodExLogo?.let { config.foodExLogo = it }
        updateTo.startOfSemester?.let { config.startOfSemester = it }
        updateTo.endOfSemester?.let { config.endOfSemester = it }

        return ConfigurationDto(configurationRepository.save(config))
    }

    /**
     * READ (may write-on-miss via get() to seed default config).
     * Not marked readOnly so seed can persist when missing.
     */
    @Transactional(readOnly = false)
    fun getHomepage(): HomepageDto {
        val config = get()

        val activeMembers = userRepository.findUserEntitiesByIsActiveTrue()
            .filter { it.role != Role.GUEST }
            .map { UserDto(it) }

        return HomepageDto(
            feelingOfTheWeek = config.feelingOfTheWeek,
            foodExLogo = config.foodExLogo,
            activeMembers = activeMembers,
            upcomingOpenings = openingRequestService.getUpcomingOpeningRequestsByIsAcceptedTrue()
        )
    }
}
