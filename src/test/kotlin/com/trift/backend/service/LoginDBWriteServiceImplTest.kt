package com.trift.backend.service

import com.trift.backend.domain.Authority
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
class LoginDBWriteServiceImplTest {

    @Autowired
    lateinit var loginDbWriteService: LoginDBWriteService

    @Test
    fun loginViaNidOrSignUp() {
        val firstUser = loginDbWriteService.loginViaNidOrSignUp(Authority.NAVER, "nid")
        val secondUser = loginDbWriteService.loginViaNidOrSignUp(Authority.NAVER, "nid")

        assertEquals(firstUser.userId, secondUser.userId)
    }
}