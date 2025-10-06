package hu.kirdev.foodex.config

import hu.kirdev.foodex.model.ShiftEntity
import hu.kirdev.foodex.repository.ShiftRepository
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import java.time.LocalDateTime

@Configuration
@Profile("test-shift")
class ShiftTestConfig {
    @Bean
    fun initShiftRepository(shiftRepository: ShiftRepository): CommandLineRunner {
        return CommandLineRunner {
            val shift1 = shiftRepository.save(
                ShiftEntity(
                    cookingClubId = 1,
                    maxMembers = 20,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusDays(1),
                    place = "-1 konyha",
                    members = listOf(1, 2),
                    newbies = emptyList(),
                    comment = "meg nem kezdodott el"
                )
            )
            val shift2 = shiftRepository.save(
                ShiftEntity(
                    cookingClubId = 2,
                    maxMembers = 10,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "13. konyha",
                    members = listOf(1, 2),
                    newbies = listOf(5),
                    comment = "ez a muszak eppen tart"
                )
            )
            val shift3 = shiftRepository.save(
                ShiftEntity(
                    cookingClubId = 3,
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "10. konyha",
                    members = listOf(1, 4),
                    newbies = listOf(5, 6),
                    comment = "ez a muszak eppen tart"
                )
            )
            val shift4 = shiftRepository.save(
                ShiftEntity(
                    cookingClubId = 1,
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusHours(1),
                    place = "8. konyha",
                    members = listOf(1, 2, 4),
                    newbies = listOf(5, 6),
                    comment = "ez a muszak eppen tart es BETELT"
                )
            )
            val shift5 = shiftRepository.save(
                ShiftEntity(
                    cookingClubId = 4,
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusHours(20),
                    closing = LocalDateTime.now().minusHours(15),
                    place = "10. konyha",
                    members = listOf(1, 2),
                    newbies = listOf(5, 6),
                    comment = "LEJART"
                )
            )
            val shift6 = shiftRepository.save(
                ShiftEntity(
                    cookingClubId = 4,
                    maxMembers = 3,
                    opening = LocalDateTime.now().minusMonths(6).minusHours(4),
                    closing = LocalDateTime.now().minusMonths(6),
                    place = "10. konyha",
                    members = listOf(1, 2),
                    newbies = listOf(5, 6),
                    comment = "ELOZO FELEVBOL VAN!!!"
                )
            )
        }
    }
}