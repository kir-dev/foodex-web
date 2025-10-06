package hu.kirdev.foodex.config

import hu.kirdev.foodex.model.CookingClubEntity
import hu.kirdev.foodex.repository.CookingClubRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner

@Configuration
@Profile("test-cookingclub")
class CookingClubTestConfig {
    @Bean
    fun initCookingClubRepository(cookingClubRepository: CookingClubRepository): CommandLineRunner {
        return CommandLineRunner {
            val club1 = cookingClubRepository.save(
                CookingClubEntity(
                    name = "Americano-0",
                    leaders = listOf(0)
                )
            )
            val club2 = cookingClubRepository.save(
                CookingClubEntity(
                    name = "Langosch-1",
                    leaders = listOf(0, 1)
                )
            )
        }
    }
}
