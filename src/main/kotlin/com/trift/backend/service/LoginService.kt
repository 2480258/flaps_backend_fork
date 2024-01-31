package com.trift.backend.service

import com.trift.backend.domain.Authority
import com.trift.backend.domain.Role
import com.trift.backend.domain.TriftUser
import com.trift.backend.security.AccessToken
import com.trift.backend.security.RefreshToken
import com.trift.backend.security.TokenDTO
import com.trift.backend.security.TokenSubject
import com.trift.backend.security.jwt.TokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

interface LoginService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun projectTokenLogin(projectId: Long) : AccessToken

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun projectWriteTokenLogin(projectId: Long) : AccessToken

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun guestLogin(): Pair<AccessToken, RefreshToken>
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun naverLogin(code: String, state: String): Pair<AccessToken, RefreshToken>
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun adminLogin() : Pair<AccessToken, RefreshToken>
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun refresh(refreshToken: RefreshToken): AccessToken
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun logout()
}

@Component
class LoginServiceImpl : LoginService {
    @Autowired
    private lateinit var tokenService: TokenService

    @Autowired
    private lateinit var naverLoginVerifyService: NaverLoginVerifyService

    @Autowired
    lateinit var loginDbWriteService: LoginDBWriteService

    @Autowired
    lateinit var authService: AuthService

    @Autowired
    lateinit var projectDBWriteService: ProjectDBWriteService

    override fun projectTokenLogin(projectId: Long): AccessToken {
        val tokenDTO = TokenDTO(TokenSubject(null, projectId, false), Role.GUEST)
        val accessToken = tokenService.createAccessToken(tokenDTO)

        return accessToken
    }

    override fun projectWriteTokenLogin(projectId: Long): AccessToken {
        val tokenDTO = TokenDTO(TokenSubject(null, projectId, true), Role.GUEST)
        val accessToken = tokenService.createAccessToken(tokenDTO)

        return accessToken
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun guestLogin(): Pair<AccessToken, RefreshToken> {
        val newUser = loginDbWriteService.writeSignUpGuestAccountAndHistory()
        val tokenDTO = TokenDTO(TokenSubject(newUser.userId!!, null, null), newUser.role!!)
        val accessToken = tokenService.createAccessToken(tokenDTO)
        val refreshToken = tokenService.createRefreshToken(tokenDTO)

        return Pair(accessToken, refreshToken)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun naverLogin(code: String, state: String): Pair<AccessToken, RefreshToken> {
        val nid = naverLoginVerifyService.getNidFromOAuthResult(code, state)
        val user = loginDbWriteService.loginViaNidOrSignUp(Authority.NAVER, nid)

        val tokenDTO = TokenDTO(TokenSubject(user.userId!!, null, null), user.role!!)
        val accessToken = tokenService.createAccessToken(tokenDTO)
        val refreshToken = tokenService.createRefreshToken(tokenDTO)

        return Pair(accessToken, refreshToken)
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun adminLogin(): Pair<AccessToken, RefreshToken> {
        val newUser = loginDbWriteService.writeSignUpAdminAccountAndHistory()
        val tokenDTO = TokenDTO(TokenSubject(newUser.userId!!, null, null), newUser.role!!)
        val accessToken = tokenService.createAccessToken(tokenDTO)
        val refreshToken = tokenService.createRefreshToken(tokenDTO)

        return Pair(accessToken, refreshToken)
    }
    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun refresh(refreshToken: RefreshToken): AccessToken {
        return tokenService.createAccessToken(refreshToken)
    }
    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun logout() {
        val user = authService.getLoginUser()
        tokenService.removeRefreshToken(user.getUserIdOrThrow())

        if(user.role == Role.GUEST) {
            projectDBWriteService.deleteAllProjectByUserId(user.getUserIdOrThrow())
        }
    }
}