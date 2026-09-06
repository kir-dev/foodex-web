package hu.kirdev.foodex.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Logs every HTTP request after it completes. Severity follows the outcome:
 * ERROR for 5xx, WARN for 4xx, INFO for mutating 2xx, DEBUG for successful reads.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class HttpRequestLoggingFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-ui") ||
            path == "/swagger-ui.html"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val started = System.nanoTime()
        try {
            filterChain.doFilter(request, response)
        } finally {
            logRequest(request, response, started)
        }
    }

    private fun logRequest(request: HttpServletRequest, response: HttpServletResponse, started: Long) {
        val status = response.status
        val durationMs = (System.nanoTime() - started) / 1_000_000
        val user = SecurityContextHolder.getContext().authentication?.name ?: "anonymous"
        val query = request.queryString?.let { "?$it" } ?: ""
        val message = "HTTP {} {}{} status={} durationMs={} user={}"
        val args = arrayOf(request.method, request.requestURI, query, status, durationMs, user)

        when {
            status >= 500 -> log.error(message, *args)
            status >= 400 -> log.warn(message, *args)
            request.method.equals("GET", ignoreCase = true) ||
                request.method.equals("HEAD", ignoreCase = true) -> log.debug(message, *args)
            else -> log.info(message, *args)
        }
    }
}
