package hu.kirdev.foodex.service

import hu.kirdev.foodex.model.ConfigurationEntity
import hu.kirdev.foodex.repository.ConfigurationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ConfigurationService (private val configurationRepository: ConfigurationRepository) {
    @Transactional(readOnly = false)
    fun get() : ConfigurationEntity {
        val config = configurationRepository.findTopByOrderByIdDesc()

        if (config == null) {
            val configuration = ConfigurationEntity(
                id = 0L,
                feelingOfTheWeek = "Feeling of the week :)",
                foodExLogo = "https://images.immediate.co.uk/production/volatile/sites/30/2020/08/chorizo-mozarella-gnocchi-bake-cropped-9ab73a3.jpg",
                startOfSemester = LocalDateTime.of(2025, 9, 1, 0, 0),
                endOfSemester = LocalDateTime.of(2026, 2, 8, 23, 59),
            )
            return configurationRepository.save(configuration)
        }
        return config
    }

    @Transactional(readOnly = false)
    fun updateConfiguration(config: ConfigurationEntity) : ConfigurationEntity {
        val configuration = get()
        configuration.startOfSemester = config.startOfSemester
        configuration.endOfSemester = config.endOfSemester
        configuration.foodExLogo  = config.foodExLogo
        configuration.feelingOfTheWeek = config.feelingOfTheWeek

        return configurationRepository.save(configuration)
    }
}