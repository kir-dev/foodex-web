package hu.kirdev.foodex.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver
import org.springframework.web.server.ResponseStatusException

/**
 * Logs exceptions without changing the API error body. Returns null so Spring's
 * default [ResponseStatusException] handling still runs.
 */
@Component
class ApiExceptionLogger : AbstractHandlerExceptionResolver() {

    private val log = LoggerFactory.getLogger(javaClass)

    init {
        order = Ordered.HIGHEST_PRECEDENCE
    }

    override fun doResolveException(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?,
        ex: Exception,
    ): ModelAndView? {
        when (ex) {
            is ResponseStatusException -> {
                val status = ex.statusCode.value()
                if (ex.statusCode.is5xxServerError) {
                    log.error("HTTP {} {} {} : {}", request.method, request.requestURI, status, ex.reason, ex)
                } else {
                    log.warn("HTTP {} {} {} : {}", request.method, request.requestURI, status, ex.reason)
                }
            }
            else -> log.error("Unhandled exception on {} {}", request.method, request.requestURI, ex)
        }
        return null
    }
}
