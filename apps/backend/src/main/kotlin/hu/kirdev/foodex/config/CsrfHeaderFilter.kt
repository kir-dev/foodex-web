package hu.kirdev.foodex.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Copies the CSRF token into the X-XSRF-TOKEN response header so a cross-origin
 * SPA can read it (document.cookie cannot see the backend-origin cookie).
 */
class CsrfHeaderFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val csrf = (request.getAttribute(CsrfToken::class.java.name) as? CsrfToken)
            ?: (request.getAttribute("_csrf") as? CsrfToken)
        csrf?.token?.let { token ->
            response.setHeader(csrf.headerName, token)
        }
        filterChain.doFilter(request, response)
    }
}
