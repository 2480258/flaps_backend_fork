package com.trift.backend.service

import com.trift.backend.domain.*
import com.trift.backend.repository.UserHistoryRepository
import com.trift.backend.repository.UserRepository
import com.trift.backend.security.RefreshToken

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

interface LoginDBWriteService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun writeSignUpNidAccountAndHistory(userAuthority: Authority, userNid: String): TriftUser
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun writeSignUpGuestAccountAndHistory(): TriftUser
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun loginViaNidOrSignUp(userAuthority: Authority, userNid: String): TriftUser
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun registerRefreshTokenAndWriteLogIn(userId: Long, refreshToken: RefreshToken)
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun writeSignUpAdminAccountAndHistory(): TriftUser
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun isVaildRefreshTokenAndWriteSignIn(userId: Long, refreshToken: RefreshToken): Boolean
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun revokeRefreshToken(userId: Long)
}

@Component
class LoginDBWriteServiceImpl : LoginDBWriteService {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userHistoryRepository: UserHistoryRepository

    private val actionSignUp = "Sign-up"
    private val actionSignIn = "Sign-in"
    private val actionRefresh = "Refresh"
    private val actionLogout = "Logout"

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun writeSignUpNidAccountAndHistory(userAuthority: Authority, userNid: String): TriftUser {
        val user = TriftUser().apply {
            this.userEmail = userNid
            this.authority = userAuthority
            this.role = Role.MEMBER
            this.status = Status.OK
        }

        userRepository.save(user)


        val history = TriftUserHistory().apply {
            this.triftUser = user
            this.action = actionSignUp
        }

        userHistoryRepository.save(history)

        return user
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun writeSignUpGuestAccountAndHistory(): TriftUser {
        val user = TriftUser().apply {
            this.authority = Authority.GUEST
            this.role = Role.GUEST
            this.status = Status.OK
        }

        userRepository.save(user)
        return user
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun writeSignUpAdminAccountAndHistory(): TriftUser {
        val user = TriftUser().apply {
            this.authority = Authority.GUEST
            this.role = Role.ADMIN
            this.status = Status.OK
        }

        userRepository.save(user)


        val history = TriftUserHistory().apply {
            this.triftUser = user
            this.action = actionSignUp
        }

        userHistoryRepository.save(history)

        return user
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun loginViaNidOrSignUp(userAuthority: Authority, userNid: String): TriftUser {
        var user = userRepository.findByUserEmail(userNid)
        if (user == null) {
            user = writeSignUpNidAccountAndHistory(userAuthority, userNid)
        }
        return user
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun registerRefreshTokenAndWriteLogIn(userId: Long, refreshToken: RefreshToken) {
        val user = userRepository.findByuserId(userId)

        val history = TriftUserHistory().apply {
            this.triftUser = user
            this.action = actionSignIn
        }

        userHistoryRepository.save(history)
        user!!.refreshToken = refreshToken.refreshToken
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun isVaildRefreshTokenAndWriteSignIn(userId: Long, refreshToken: RefreshToken): Boolean {
        val user = userRepository.findByuserId(userId)

        val history = TriftUserHistory().apply {
            this.triftUser = user
            this.action = actionRefresh
        }

        userHistoryRepository.save(history)

        return user?.refreshToken == refreshToken.refreshToken
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun revokeRefreshToken(userId: Long) {
        val user = userRepository.findByuserId(userId)

        val history = TriftUserHistory().apply {
            this.triftUser = user
            this.action = actionLogout
        }

        userHistoryRepository.save(history)

        user?.refreshToken = null
    }
}