package hu.kirdev.foodex.config

import hu.kirdev.foodex.oidcuser.FoodExOidcUserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class WebSecurityConfig(
    @param:Value("\${foodex.frontend-url:http://localhost:3000}")
    private val frontendUrl: String,
) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(
        http: HttpSecurity,
        foodexOidcUserService: FoodExOidcUserService,
    ): SecurityFilterChain {
        // SPA-friendly CSRF: token in cookie XSRF-TOKEN, client sends header X-XSRF-TOKEN
        val csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse()
        val csrfRequestHandler = CsrfTokenRequestAttributeHandler().apply {
            // Deferred tokens work poorly with some SPAs; use plain attribute handler
            setCsrfRequestAttributeName("_csrf")
        }

        http
            .csrf { csrf ->
                csrf
                    .csrfTokenRepository(csrfRepository)
                    .csrfTokenRequestHandler(csrfRequestHandler)
            }
            .cors { }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers(
                        "/",
                        "/api/homepage",
                        "/error",
                    ).permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                    ).permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/api/config").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/cooking-clubs").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/cooking-clubs/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/cooking-clubs/**").hasRole("ADMIN")
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().authenticated()
            }
            // SPA-friendly: return 401 for unauthenticated API calls instead of HTML login redirect
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint { request, response, _ ->
                    if (request.requestURI.startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                    } else {
                        response.sendRedirect("/oauth2/authorization/authsch")
                    }
                }
            }
            .oauth2Login { oauth2 ->
                oauth2
                    .userInfoEndpoint { endpoint ->
                        endpoint.oidcUserService(foodexOidcUserService)
                    }
                    // After AuthSCH login, land on the SPA (not JSON homepage)
                    .defaultSuccessUrl(frontendUrl, true)
            }
            .logout { logout ->
                logout.logoutSuccessUrl(frontendUrl)
            }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(frontendUrl, "http://localhost:3000")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("X-XSRF-TOKEN")
            allowCredentials = true
            maxAge = 3600L
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
