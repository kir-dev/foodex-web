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
        }
    }

}