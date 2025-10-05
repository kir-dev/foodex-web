package hu.kirdev.foodex.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class WebSecurityConfig {
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain? {
        http
            .authorizeHttpRequests(Customizer { requests ->
                requests
                    .requestMatchers(
                        "/",
                        "/api/**",
                        "/v3/api-docs",
                        "/swagger-ui/*",
                        "/v3/api-docs/swagger-config").permitAll()
                    .anyRequest().authenticated()
            }
            )
            .formLogin(Customizer { form ->
                form
                    .loginPage("/login")
                    .permitAll()
            }
            )
            .logout(Customizer { logout -> logout.permitAll() })

        return http.build()
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
