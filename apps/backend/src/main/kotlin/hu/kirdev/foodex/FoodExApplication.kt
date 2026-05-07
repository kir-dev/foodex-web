package hu.kirdev.foodex

//import hu.kirdev.foodex.testconfig.CookingClubTestConfig
//import hu.kirdev.foodex.testconfig.OpeningRequestTestConfig
//import hu.kirdev.foodex.testconfig.ShiftTestConfig
//import hu.kirdev.foodex.testconfig.UserTestConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
//@Import(
//    UserTestConfig::class,
//    ShiftTestConfig::class,
//    OpeningRequestTestConfig::class,
//    CookingClubTestConfig::class,
//    ConfigurationTestConfig::class
//)
class FoodExApplication

fun main(args: Array<String>) {
    val profiles = arrayOf("test-user", "test-shift", "test-opening-request", "test-cookingclub", "test-config")
	runApplication<FoodExApplication>(*args) {
        setAdditionalProfiles(*profiles)
    }
}
