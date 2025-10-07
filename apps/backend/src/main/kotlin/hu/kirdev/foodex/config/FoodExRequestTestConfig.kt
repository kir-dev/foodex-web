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
                    userId = 3,
                    cookingClubId = 1,
                    opening = LocalDateTime.now().plusDays(1),
                    closing = LocalDateTime.now().plusDays(1).plusHours(4),
                    place = "10. konyha",
                    description = "ez egy leiras americano"
                )
            )
            val request2 = foodExRequestRepository.save(
                FoodExRequestEntity(
                    userId = 4,
                    cookingClubId = 1,
                    opening = LocalDateTime.now().plusDays(2),
                    closing = LocalDateTime.now().plusDays(2).plusHours(2),
                    place = "11. konyha",
                    description = "ez is egy leiras langosch"
                )
            )
            val request3 = foodExRequestRepository.save(
                FoodExRequestEntity(
                    userId = 0,
                    cookingClubId = 2,
                    opening = LocalDateTime.now().minusYears(1),
                    closing = LocalDateTime.now().minusYears(1).plusHours(2),
                    place = "18. konyha",
                    description = "1 evel ezelottrol request, pizzasch"
                )
            )
            val request4 = foodExRequestRepository.save(
                FoodExRequestEntity(
                    userId = 4,
                    cookingClubId = 1,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(2),
                    place = "11. konyha",
                    description = "mar elkezdodott langosch"
                )
            )
            val request5 = foodExRequestRepository.save(
                FoodExRequestEntity(
                    userId = 4,
                    cookingClubId = 1,
                    opening = LocalDateTime.now().minusMonths(6),
                    closing = LocalDateTime.now().minusMonths(6).plusHours(2),
                    place = "4. konyha",
                    description = "ELMULT FELEVBOL VAN!!!"
                )
            )
        }
    }
}