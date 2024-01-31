package com.trift.backend.security.jwt

import com.trift.backend.domain.Authority
import com.trift.backend.domain.Role
import com.trift.backend.domain.Status
import com.trift.backend.domain.TriftUser
import com.trift.backend.repository.UserRepository
import com.trift.backend.security.TokenDTO
import com.trift.backend.security.TokenSubject
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = [
    "jwt.access_secret=123",
    "jwt.refresh_secret=456",
    "jwt.access_expire=100000",
    "jwt.refresh_expire=1000000"])
class TokenServiceTestWithLargeExpireTime {

    @Autowired
    lateinit var tokenService: TokenService

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun createAccessTokenAndVerifyWithLargeExpire() {
        // Given

        val tokenDTO = TokenDTO(TokenSubject(123, null, null), Role.MEMBER)
        val access = tokenService.createAccessToken(tokenDTO)

        // When
        val verify = tokenService.verifyAccessToken(access)

        // Then
        assertEquals(123, verify.userId)
        assertEquals(Role.MEMBER, verify.role)
    }

    @Test
    fun createRefreshTokenAndVerifyWithLargeExpire() {
        // Given
        val user = TriftUser()
        user.status = Status.OK
        user.authority = Authority.GUEST
        user.role = Role.GUEST

        userRepository.save(user)
        val tokenDTO = TokenDTO(TokenSubject(user.userId, null, null),  Role.MEMBER)
        val refresh = tokenService.createRefreshToken(tokenDTO)

        // When
        val access = tokenService.createAccessToken(refresh)
        val verify = tokenService.verifyAccessToken(access)

        // Then
        assertEquals(user.userId, verify.userId)
        assertEquals(Role.MEMBER, verify.role)
    }
}

@SpringBootTest
@TestPropertySource(properties = [
    "jwt.access_secret=123",
    "jwt.refresh_secret=456",
    "jwt.access_expire=1",
    "jwt.refresh_expire=1"])
class TokenServiceTestWithSmallExpireTime {

    @Autowired
    lateinit var tokenService: TokenService


    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun createAccessTokenAndVerifyWithSmallExpire() {
        // Given

        val tokenDTO = TokenDTO(TokenSubject(123, null, null), Role.MEMBER)
        val access = tokenService.createAccessToken(tokenDTO)

        // When
        assertThrows(Exception::class.java) {
            tokenService.verifyAccessToken(access)
        }
    }

    @Test
    fun createRefreshTokenAndVerifyWithSmallExpire() {
        // Given
        val user = TriftUser()
        user.status = Status.OK
        user.authority = Authority.GUEST
        user.role = Role.GUEST

        userRepository.save(user)
        val tokenDTO = TokenDTO(TokenSubject(user.userId, null, null),  Role.MEMBER)
        val refresh = tokenService.createRefreshToken(tokenDTO)

        // When
        assertThrows(Exception::class.java) {
            tokenService.createAccessToken(refresh)
        }
    }
}
