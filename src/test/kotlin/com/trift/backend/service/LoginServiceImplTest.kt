package com.trift.backend.service

import com.trift.backend.domain.Authority
import com.trift.backend.domain.Role
import com.trift.backend.domain.Status
import com.trift.backend.repository.UserRepository
import com.trift.backend.security.jwt.TokenService
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class LoginServiceImplTest {

    @Autowired
    lateinit var loginService: LoginService

    @Autowired
    lateinit var tokenService: TokenService

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    @Transactional
    fun guestLogin() {
        val tokens = loginService.guestLogin()
        val tokenDTO = tokenService.verifyAccessToken(tokens.first)

        val user = userRepository.findByuserId(tokenDTO.userId!!)!!

        assertNotNull(user.userId)
        assertNull(user.userEmail)
        assertEquals(user.authority, Authority.GUEST)
        assertEquals(user.role, Role.GUEST)
        assertEquals(user.status, Status.OK)
    }
}