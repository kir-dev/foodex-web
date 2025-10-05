package hu.kirdev.foodex.config

import hu.kirdev.foodex.model.FoodExRequestEntity
import hu.kirdev.foodex.repository.FoodExRequestRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import java.time.LocalDateTime

@Configuration
@Profile("test-foodex-request")
class FoodExRequestTestConfig {
    @Bean
    fun initFoodExRequestRepository(foodExRequestRepository: FoodExRequestRepository): CommandLineRunner {
        return CommandLineRunner {
            val request1 = foodExRequestRepository.save(
                FoodExRequestEntity(
                    userId = 0L,
                    cookingClubId = 0,
                    opening = LocalDateTime.now().plusDays(1),
                    closing = LocalDateTime.now().plusDays(1).plusHours(4),
                    place = "10. konyha",
                    description = "ez egy leiras"
                )
            )
            val request2 = foodExRequestRepository.save(
                FoodExRequestEntity(
                    userId = 1L,
                    cookingClubId = 1,
                    opening = LocalDateTime.now().plusDays(2),
                    closing = LocalDateTime.now().plusDays(2).plusHours(2),
                    place = "11. konyha",
                    description = "ez is egy leiras"
                )
            )
        }
    }
}