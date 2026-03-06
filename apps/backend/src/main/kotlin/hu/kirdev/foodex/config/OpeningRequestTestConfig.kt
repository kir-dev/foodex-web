package hu.kirdev.foodex.config

import hu.kirdev.foodex.model.OpeningRequestEntity
import hu.kirdev.foodex.repository.OpeningRequestRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import java.time.LocalDateTime

@Configuration
@Profile("test-opening-request")
class OpeningRequestTestConfig {
    @Bean
    fun initOpeningRequestRepository(openingRequestRepository: OpeningRequestRepository): CommandLineRunner {
        return CommandLineRunner {
            val request1 = openingRequestRepository.save(
                OpeningRequestEntity(
                    userId = 3,
                    cookingClubId = 403,
                    opening = LocalDateTime.now().plusDays(1),
                    closing = LocalDateTime.now().plusDays(1).plusHours(4),
                    place = "10. konyha",
                    description = "ez egy leiras americano"
                )
            )
            val request2 = openingRequestRepository.save(
                OpeningRequestEntity(
                    userId = 4,
                    cookingClubId = 403,
                    opening = LocalDateTime.now().plusDays(2),
                    closing = LocalDateTime.now().plusDays(2).plusHours(2),
                    place = "11. konyha",
                    description = "ez is egy leiras langosch"
                )
            )
            val request3 = openingRequestRepository.save(
                OpeningRequestEntity(
                    userId = 0,
                    cookingClubId = 473,
                    opening = LocalDateTime.now().minusYears(1),
                    closing = LocalDateTime.now().minusYears(1).plusHours(2),
                    place = "18. konyha",
                    description = "1 evel ezelottrol request, pizzasch"
                )
            )
            val request4 = openingRequestRepository.save(
                OpeningRequestEntity(
                    userId = 4,
                    cookingClubId = 403,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(2),
                    place = "11. konyha",
                    description = "mar elkezdodott langosch"
                )
            )
            val request5 = openingRequestRepository.save(
                OpeningRequestEntity(
                    userId = 4,
                    cookingClubId = 403,
                    opening = LocalDateTime.now().minusMonths(6),
                    closing = LocalDateTime.now().minusMonths(6).plusHours(2),
                    place = "4. konyha",
                    description = "ELMULT FELEVBOL VAN!!!"
                )
            )
        }
    }
}