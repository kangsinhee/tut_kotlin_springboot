package sinhee.kang.tutorial.global.security.jwt

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import sinhee.kang.tutorial.global.security.jwt.enums.TokenType

class JwtTokenFilter(
    private val tokenProvider: JwtTokenProvider
): OncePerRequestFilter() {

    override fun doFilterInternal(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, filterChain: FilterChain) {
        val accessToken: String? = try {
            httpServletRequest.cookies
                .first { cookie -> cookie.name == TokenType.ACCESS.cookieName }
                .value
        } catch (e: Exception) { null }

        if (!accessToken.isNullOrEmpty() && tokenProvider.isValidateToken(accessToken))
            SecurityContextHolder.getContext()
                .authentication = tokenProvider.getAuthentication(accessToken)

        filterChain.doFilter(httpServletRequest, httpServletResponse)
    }
}
