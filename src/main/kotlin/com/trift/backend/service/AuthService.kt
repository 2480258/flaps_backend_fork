package com.trift.backend.service

import com.trift.backend.security.TokenDTO
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

interface AuthService {
    fun getLoginUser() : TokenDTO
}

@Service
class AuthServiceImpl : AuthService {
    override fun getLoginUser() : TokenDTO {
        val auth = SecurityContextHolder.getContext().authentication

        return auth.principal as TokenDTO
    }
}