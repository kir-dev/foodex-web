package hu.kirdev.foodex

import hu.kirdev.foodex.testconfig.CookingClubTestConfig
import hu.kirdev.foodex.testconfig.OpeningRequestTestConfig
import hu.kirdev.foodex.testconfig.ShiftTestConfig
import hu.kirdev.foodex.testconfig.UserTestConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(
    UserTestConfig::class,
    ShiftTestConfig::class,
    OpeningRequestTestConfig::class,
    CookingClubTestConfig::class,
)
class FoodExApplication

fun main(args: Array<String>) {
    val profiles = arrayOf("test")
	runApplication<FoodExApplication>(*args) {
        setAdditionalProfiles(*profiles)
    }
}
