package hu.kirdev.foodex.config

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ConfigurationController(
    private val configurationService: ConfigurationService,
) {

    @Operation(summary = "Get home page")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Homepage found",
                content = [Content(schema = Schema(implementation = HomepageDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Part of homepage not found"),
        ]
    )
    @GetMapping("/home")
    fun getHomepage() : ResponseEntity<HomepageDto> {
        val homePage = configurationService.getHomepage()
        return ResponseEntity.status(HttpStatus.OK).body(homePage)
    }

    @Operation(summary = "Get configuration")
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Configuration found",
            content = [Content(schema = Schema(implementation = ConfigurationDto::class))]
        )
    )
    @GetMapping("/config")
    fun getConfiguration() : ResponseEntity<ConfigurationDto>  {
        val configuration = configurationService.get()
        return ResponseEntity.status(HttpStatus.OK).body(configuration)
    }

    @Operation(summary = "Update the configuration")
    @ApiResponses(
            ApiResponse(
                responseCode = "200",
                description = "Configuration updated",
                content = [Content(schema = Schema(implementation = ConfigurationDto::class))]
            )
    )
    @PatchMapping("/config")
    fun updateConfiguration(config: UpdateConfigurationDto) : ResponseEntity<ConfigurationDto>  {
        val configuration = configurationService.updateConfiguration(config)
        return ResponseEntity.status(HttpStatus.OK).body(configuration)
    }
}