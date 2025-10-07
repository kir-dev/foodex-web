package hu.kirdev.foodex.config

import hu.kirdev.foodex.model.Role
import hu.kirdev.foodex.model.UserEntity
import hu.kirdev.foodex.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test-user")
class UserTestConfig {

    @Bean
    fun initUserRepository(userRepository: UserRepository): CommandLineRunner {
        return CommandLineRunner {
            val user1 = userRepository.save(
                UserEntity(
                    role = Role.ADMIN,
                    name = "Sali Nora",
                    nickname = "Nori",
                    email = "nori@gmail.com",
                    favouriteQuote = "I <3 FoodEx",
                    isActive = true,
                    profilePicture = null
                )
            )
            val user2 = userRepository.save(
                UserEntity(
                    role = Role.MEMBER,
                    name = "Kis Pista",
                    nickname = "Pistike",
                    email = "kis.pista@gmail.com",
                    favouriteQuote = "I <3 FoodEx too!",
                    isActive = true,
                    profilePicture = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Cat_August_2010-4.jpg/1200px-Cat_August_2010-4.jpg"
                )
            )
            val user3 = userRepository.save(
                UserEntity(
                    role = Role.GUEST,
                    name = "Langosch es Paschta korvez",
                    nickname = "langosch paschta korvez",
                    email = "langosch.paschta.korvez@gmail.com",
                    favouriteQuote = "I <3 langos es paschta",
                    isActive = true,
                    profilePicture = null
                )
            )
            val user4 = userRepository.save(
                UserEntity(
                    role = Role.MEMBER,
                    name = "Americano korvez es member",
                    nickname = "americano korvez es member",
                    email = "americano.korvez.member@gmail.com",
                    favouriteQuote = "I <3 Hamgurger es FoodEx",
                    isActive = true,
                    profilePicture = null
                )
            )
            val user5 = userRepository.save(
                UserEntity(
                    role = Role.NEWBIE,
                    name = "Langosch korvez es newbie",
                    nickname = "langosch korvez es newbie",
                    email = "langosch.korvez.newbie@gmail.com",
                    favouriteQuote = "I <3 Langosch es FoodEx",
                    isActive = true,
                    profilePicture = null
                )
            )
            val user6 = userRepository.save(
                UserEntity(
                    role = Role.NEWBIE,
                    name = "Ujonc Peter",
                    nickname = "Peti",
                    email = "peti.ujonc@gmail.com",
                    favouriteQuote = "I <3 FoodEx",
                    isActive = true,
                    profilePicture = null
                )
            )
        }
    }

}