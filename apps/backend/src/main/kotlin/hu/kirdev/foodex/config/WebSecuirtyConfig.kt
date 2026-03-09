package hu.kirdev.foodex.config



import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .csrf{it.disable()}
            .cors { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/",
                        "/api/**",
                        "/v3/api-docs",
                        "/swagger-ui/*",
                        "/v3/api-docs/swagger-config"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            // Specify your frontend origin (replace with actual frontend URL)
            allowedOrigins = listOf("http://localhost:3000", "http://localhost:8080") // Update with your frontend URL
            // Allow all necessary HTTP methods
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            // Allow all headers
            allowedHeaders = listOf("*")
            // Allow credentials if needed (set to false if not using cookies/auth headers)
            allowCredentials = true
            // Cache preflight response for 1 hour
            maxAge = 3600L
        }

        val source = UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
        return source
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user =
            User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build()

        return InMemoryUserDetailsManager(user)
    }
}


