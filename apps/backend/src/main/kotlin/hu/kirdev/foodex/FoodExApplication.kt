package hu.kirdev.foodex

import hu.kirdev.foodex.config.ConfigurationTestConfig
import hu.kirdev.foodex.config.CookingClubTestConfig
import hu.kirdev.foodex.config.FoodExRequestTestConfig
import hu.kirdev.foodex.config.ShiftTestConfig
import hu.kirdev.foodex.config.UserTestConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(
    UserTestConfig::class,
    ShiftTestConfig::class,
    FoodExRequestTestConfig::class,
    CookingClubTestConfig::class,
    ConfigurationTestConfig::class
)
class FoodExApplication

fun main(args: Array<String>) {
    val profiles = arrayOf("test-user", "test-shift", "test-foodex-request", "test-cookingclub", "test-config")
	runApplication<FoodExApplication>(*args) {
        setAdditionalProfiles(*profiles)
    }
}
