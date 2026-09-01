package hu.kirdev.foodex.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class TimeConfig {

    @Bean
    fun clock(): Clock = Clock.system(APP_ZONE)

    companion object {
        val APP_ZONE: ZoneId = ZoneId.of("Europe/Budapest")
    }
}
