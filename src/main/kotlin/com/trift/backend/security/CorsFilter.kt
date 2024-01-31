package com.trift.backend.security

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component


@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsFilter : Filter {
    override fun init(filterConfig: FilterConfig?) {
    }

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val request = req as HttpServletRequest
        val response = res as HttpServletResponse
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"))
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT")
        response.setHeader("Access-Control-Max-Age", "3600")
        response.setHeader(
            "Access-Control-Allow-Headers",
            "Origin, X-Requested-With, Content-Type, Accept, Authorization"
        )
        if ("OPTIONS".equals(request.method, ignoreCase = true)) {
            response.status = HttpServletResponse.SC_OK
        } else {
            chain.doFilter(req, res)
        }
    }

    override fun destroy() {}
}