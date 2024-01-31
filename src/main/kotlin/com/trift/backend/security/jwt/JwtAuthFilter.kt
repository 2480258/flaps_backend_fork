package com.trift.backend.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.trift.backend.security.AccessToken
import com.trift.backend.security.TokenDTO
import com.trift.backend.web.dto.CommonResponseWithoutData
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.lang.UnsupportedOperationException
import java.util.List


@Component
class JwtAuthFilter : OncePerRequestFilter() {

    var tokenService: TokenService? = null

    private fun prepareBeansIfTheyDont(request: HttpServletRequest) {
        if(tokenService == null) {
            tokenService = WebApplicationContextUtils.getWebApplicationContext(request.servletContext)!!.getBean(TokenService::class.java)
        }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        prepareBeansIfTheyDont(request)

        val token: String? = request.getHeader("Authorization") ?: request.getHeader("authorization")

        if (!token.isNullOrBlank()) {
            val tokenDTO = try {
                val extractedToken = convertAuthorizationHeaderToJWT(token)

                tokenService!!.verifyAccessToken(AccessToken(extractedToken))
            } catch (e: Exception) {
                logger.warn("JWT 인증이 거절되었습니다", e)
                response.status = HttpStatus.FORBIDDEN.value()
                response.writer.write(convertObjectToJson(CommonResponseWithoutData(HttpStatus.FORBIDDEN.value(), "JWT Token 인증이 거절되었습니다.", 0)))

                return
            }
            val auth = getAuthentication(tokenDTO)
            SecurityContextHolder.getContext().authentication = auth
        }

        filterChain.doFilter(request, response)
    }

    private fun convertAuthorizationHeaderToJWT(authHeader: String): String {
        val headers = authHeader.trim().split(" ")

        if((headers.count() != 2) or (headers.firstOrNull() != "Bearer")) {
            throw UnsupportedOperationException("Header is not starting with bearer")
        }

        return headers[1]
    }

    private fun getAuthentication(user: TokenDTO): Authentication {
        return UsernamePasswordAuthenticationToken(
            user, "",
            List.of(SimpleGrantedAuthority("ROLE_" + user.role.name))
        )
    }

    private fun <T> convertObjectToJson(resp: T): String {
        val mapper = ObjectMapper()
        return mapper.writeValueAsString(resp)
    }
}