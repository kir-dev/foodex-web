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
            // Pizzásch
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 223,
                    name = "Pizzasch-223",
                    leaders = listOf(1)
                )
            )

            // Americano
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 403,
                    name = "Americano-403",
                    leaders = listOf(1, 4)
                )
            )

            // Vödör
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 179,
                    name = "Vödör-179",
                    leaders = listOf(1)
                )
            )

            // LángoSCH
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 473,
                    name = "Langosch-473",
                    leaders = listOf(1, 3, 5)
                )
            )

            // Kakas
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 31,
                    name = "Kakas-31",
                    leaders = listOf(1)
                )
            )

            // Paschta;
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 528,
                    name = "Paschta;-528",
                    leaders = listOf(1, 3)
                )
            )

            // Palacsintázó
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 395,
                    name = "Palacsintázó-395",
                    leaders = listOf(1)
                )
            )

            // ReggeliSCH
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 490,
                    name = "ReggliSCH-490",
                    leaders = listOf(1)
                )
            )
        }
    }
}
