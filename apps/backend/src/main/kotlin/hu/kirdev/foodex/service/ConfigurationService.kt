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
        if (configurationRepository.findAll().isEmpty()) {
            val configuration = ConfigurationEntity(
                id = 0L,
                feelingOfTheWeek = "Feeling of the week :)",
                foodExLogo = "https://images.immediate.co.uk/production/volatile/sites/30/2020/08/chorizo-mozarella-gnocchi-bake-cropped-9ab73a3.jpg",
                startOfSemester = LocalDateTime.of(2025, 9, 1, 0, 0),
                endOfSemester = LocalDateTime.of(2026, 2, 8, 23, 59),
            )
            return configurationRepository.save(configuration)
        }
        return configurationRepository.findAll().first()
    }

    @Transactional(readOnly = true)
    fun updateConfiguration(config: ConfigurationEntity) : ConfigurationEntity {
        if (config.id != 0L) {
            throw IllegalArgumentException("ConfigurationId ${config.id} must be zero")
        }
        return configurationRepository.save(config)
    }
}