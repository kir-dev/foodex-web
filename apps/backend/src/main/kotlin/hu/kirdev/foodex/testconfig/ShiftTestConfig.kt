package hu.kirdev.foodex.testconfig

import hu.kirdev.foodex.cookingclub.CookingClubRepository
import hu.kirdev.foodex.shift.ShiftEntity
import hu.kirdev.foodex.shift.ShiftRepository
import hu.kirdev.foodex.user.UserEntity
import hu.kirdev.foodex.user.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime

@Configuration
@Profile("test")
class ShiftTestConfig {
    @Bean
    fun initShiftRepository(
        shiftRepository: ShiftRepository,
        userRepository: UserRepository,
        cookingClubRepository: CookingClubRepository,
    ): CommandLineRunner {
        return CommandLineRunner {
            val user1 = userRepository.findById(1).orElseThrow()
            val user2 = userRepository.findById(2).orElseThrow()
            val user3 = userRepository.findById(3).orElseThrow()
            val user4 = userRepository.findById(4).orElseThrow()
            val user5 = userRepository.findById(5).orElseThrow()
            val user6 = userRepository.findById(6).orElseThrow()

            val shift1 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = cookingClubRepository.findById(403).orElseThrow(),
                    maxMembers = 20,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusDays(1),
                    place = "-1 konyha",
                    comment = "meg nem kezdodott el",
                    workers = mutableListOf<UserEntity>(user1, user2),
                )
            )
            val shift2 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = cookingClubRepository.findById(473).orElseThrow(),
                    maxMembers = 10,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "13. konyha",
                    comment = "ez a muszak eppen tart",
                    workers = mutableListOf<UserEntity>(user1, user2, user5),
                )
            )
            val shift3 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = cookingClubRepository.findById(223).orElseThrow(),
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "10. konyha",
                    comment = "ez a muszak eppen tart",
                    workers = mutableListOf<UserEntity>(user1, user4, user5, user6),
                )
            )
            val shift4 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = cookingClubRepository.findById(403).orElseThrow(),
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "8. konyha",
                    comment = "ez a muszak eppen tart es BETELT",
                    workers = mutableListOf(user1, user2, user4, user5, user6),
                )
            )
            val shift5 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = cookingClubRepository.findById(528).orElseThrow(),
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(20),
                    closing = LocalDateTime.now().minusHours(15),
                    place = "10. konyha",
                    comment = "LEJART",
                    workers = mutableListOf<UserEntity>(user1, user2, user5, user6),
                )
            )
            val shift6 = shiftRepository.save(
                ShiftEntity(
                    cookingClub = cookingClubRepository.findById(528).orElseThrow(),
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusMonths(6).minusHours(4),
                    closing = LocalDateTime.now().minusMonths(6),
                    place = "10. konyha",
                    comment = "ELOZO FELEVBOL VAN!!!",
                    workers = mutableListOf<UserEntity>(user1, user2, user5, user6),
                )
            )
        }
    }
}