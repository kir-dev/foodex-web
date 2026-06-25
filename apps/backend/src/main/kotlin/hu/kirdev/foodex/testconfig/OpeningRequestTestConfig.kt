package hu.kirdev.foodex.testconfig

import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.openingrequest.OpeningRequestEntity
import hu.kirdev.foodex.openingrequest.OpeningRequestRepository
import hu.kirdev.foodex.user.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime

@Configuration
@Profile("test")
class OpeningRequestTestConfig {
    @Bean
    fun initOpeningRequestRepository(
        openingRequestRepository: OpeningRequestRepository,
        userRepository: UserRepository,
        cookingClubRepository: CookingClubRepository,
    ): CommandLineRunner {

        return CommandLineRunner {
            val request1 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = userRepository.findById(4).orElseThrow(),
                    cookingClub = cookingClubRepository.findById(403).orElseThrow(),
                    opening = LocalDateTime.now().plusDays(1),
                    closing = LocalDateTime.now().plusDays(1).plusHours(4),
                    place = "10. konyha",
                    description = "ez egy leiras americano"
                )
            )
            val request2 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = userRepository.findById(5).orElseThrow(),
                    cookingClub = cookingClubRepository.findById(403).orElseThrow(),
                    opening = LocalDateTime.now().plusDays(2),
                    closing = LocalDateTime.now().plusDays(2).plusHours(2),
                    place = "11. konyha",
                    description = "ez is egy leiras langosch"
                )
            )
            val request3 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = userRepository.findById(1).orElseThrow(),
                    cookingClub = cookingClubRepository.findById(473).orElseThrow(),
                    opening = LocalDateTime.now().minusYears(1),
                    closing = LocalDateTime.now().minusYears(1).plusHours(2),
                    place = "18. konyha",
                    description = "1 evel ezelottrol request, pizzasch"
                )
            )
            val request4 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = userRepository.findById(5).orElseThrow(),
                    cookingClub = cookingClubRepository.findById(403).orElseThrow(),
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(2),
                    place = "11. konyha",
                    description = "mar elkezdodott langosch"
                )
            )
            val request5 = openingRequestRepository.save(
                OpeningRequestEntity(
                    user = userRepository.findById(5).orElseThrow(),
                    cookingClub = cookingClubRepository.findById(403).orElseThrow(),
                    opening = LocalDateTime.now().minusMonths(6),
                    closing = LocalDateTime.now().minusMonths(6).plusHours(2),
                    place = "4. konyha",
                    description = "ELMULT FELEVBOL VAN!!!"
                )
            )
        }
    }
}