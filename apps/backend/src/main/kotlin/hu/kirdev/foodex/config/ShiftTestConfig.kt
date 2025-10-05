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
                    cookingClubId = 0L,
                    maxMembers = 20,
                    opening = LocalDateTime.now().minusHours(1),
                    closing = LocalDateTime.now().plusDays(1),
                    place = "-1 konyha",
                    members = emptyList(),
                    newbies = emptyList(),
                    comment = "ez egy komment"
                )
            )
            val shift2 = shiftRepository.save(
                ShiftEntity(
                    cookingClubId = 1L,
                    maxMembers = 10,
                    opening = LocalDateTime.of(2025, 9, 3, 8, 0, 0, 0),
                    closing = LocalDateTime.of(2025, 9, 3, 20, 0, 0, 0),
                    place = "13. konyha",
                    members = emptyList(),
                    newbies = emptyList(),
                    comment = "ez egy masik komment"
                )
            )
        }
    }
}