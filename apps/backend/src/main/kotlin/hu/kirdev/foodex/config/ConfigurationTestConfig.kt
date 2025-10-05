package hu.kirdev.foodex.config

import hu.kirdev.foodex.model.ConfigurationEntity
import hu.kirdev.foodex.repository.ConfigurationRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import java.time.LocalDateTime

@Configuration
@Profile("test-config")
class ConfigurationTestConfig {
    @Bean
    fun initConfigurationRepository(configurationRepository: ConfigurationRepository): CommandLineRunner {
        return CommandLineRunner {
            val config1 = configurationRepository.save(
                ConfigurationEntity(
                    feelingOfTheWeek = "Sunshine",
                    foodExLogo = "https://upload.wikimedia.org/wikipedia/commons/thumb/2/24/LEGO_logo.svg/512px-LEGO_logo.svg.png",
                    startOfSemester = LocalDateTime.of(2025, 9, 1, 0, 0, 0),
                    endOfSemester = LocalDateTime.of(2026, 2, 1, 23, 59, 59)
                )
            )
        }
    }
}