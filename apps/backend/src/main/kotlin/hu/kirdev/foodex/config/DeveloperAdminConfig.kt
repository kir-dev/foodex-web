package hu.kirdev.foodex.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.Properties

/**
 * Loads AuthSCH internalIds that should be elevated to ADMIN on login.
 * Keys of developer-admins.properties are the IDs; values are human notes only.
 */
@Configuration
class DeveloperAdminConfig {

    @Bean
    fun developerAdminIds(): Set<String> {
        val resource = ClassPathResource("config/developer-admins.properties")
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
