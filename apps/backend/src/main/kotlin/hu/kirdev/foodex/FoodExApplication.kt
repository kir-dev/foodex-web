package hu.kirdev.foodex

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FoodExApplication

fun main(args: Array<String>) {
	runApplication<FoodExApplication>(*args)
}
