package hu.kirdev.foodex.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.Properties

/**
 * Loads AuthSCH internalIds that should be elevated to SUPERUSER on login.
 * Keys of superusers.properties are the IDs; values are human notes only.
 */
@Configuration
class SuperuserConfig {

    @Bean
    fun superuserIds(): Set<String> {
        val resource = ClassPathResource("config/superusers.properties")
        if (!resource.exists()) {
            return emptySet()
        }
        val props = Properties()
        resource.inputStream.use { props.load(it) }
        return props.stringPropertyNames()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}
