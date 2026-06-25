package hu.kirdev.foodex.testconfig

import hu.kirdev.foodex.cookingclub.CookingClubEntity
import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class CookingClubTestConfig {
    @Bean
    fun initCookingClubRepository(
        cookingClubRepository: CookingClubRepository,
        userRepository: UserRepository,
    ): CommandLineRunner {

        return CommandLineRunner {
            val user1 = userRepository.findById(1).orElseThrow()
            val user3 = userRepository.findById(3).orElseThrow()
            val user4 = userRepository.findById(4).orElseThrow()
            val user5 = userRepository.findById(5).orElseThrow()

            // Pizzásch
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 223,
                    name = "Pizzasch-223",
                    leaders = mutableListOf(user1)
                )
            )

            // Americano
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 403,
                    name = "Americano-403",
                    leaders = mutableListOf(user1, user4)
                )
            )

            // Vödör
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 179,
                    name = "Vödör-179",
                    leaders = mutableListOf(user1)
                )
            )

            // LángoSCH
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 473,
                    name = "Langosch-473",
                    leaders = mutableListOf(user1, user3, user5)
                )
            )

            // Kakas
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 31,
                    name = "Kakas-31",
                    leaders = mutableListOf(user1)
                )
            )

            // Paschta;
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 528,
                    name = "Paschta;-528",
                    leaders = mutableListOf(user1, user3)
                )
            )

            // Palacsintázó
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 395,
                    name = "Palacsintázó-395",
                    leaders = mutableListOf(user1)
                )
            )

            // ReggeliSCH
            cookingClubRepository.save(
                CookingClubEntity(
                    id = 490,
                    name = "ReggliSCH-490",
                    leaders = mutableListOf(user1)
                )
            )
        }
    }
}
