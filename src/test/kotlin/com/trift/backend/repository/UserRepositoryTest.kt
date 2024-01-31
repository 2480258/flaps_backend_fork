package com.trift.backend.repository

import com.trift.backend.domain.Authority
import com.trift.backend.domain.Role
import com.trift.backend.domain.Status
import com.trift.backend.domain.TriftUser
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    @Transactional
    fun userRepositoryFindById() {
        val user = TriftUser()
        user.status = Status.OK
        user.role = Role.GUEST
        user.authority = Authority.GUEST

        userRepository.save(user)


        val retrivedUser = userRepository.findByuserId(user.userId!!)!!

        Assertions.assertNotNull(retrivedUser.userId)
        Assertions.assertNull(retrivedUser.userEmail)
        Assertions.assertEquals(retrivedUser.authority, Authority.GUEST)
        Assertions.assertEquals(retrivedUser.role, Role.GUEST)
        Assertions.assertEquals(retrivedUser.status, Status.OK)
    }
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    @Transactional
    fun userRepositoryFindByEmail() {
        val user = TriftUser()
        user.status = Status.OK
        user.role = Role.GUEST
        user.authority = Authority.GUEST
        user.userEmail = "aaa@aaa.com"
        userRepository.save(user)


        val retrivedUser = userRepository.findByUserEmail("aaa@aaa.com")!!

        Assertions.assertNotNull(retrivedUser.userId)
        Assertions.assertEquals("aaa@aaa.com", retrivedUser.userEmail)
        Assertions.assertEquals(retrivedUser.authority, Authority.GUEST)
        Assertions.assertEquals(retrivedUser.role, Role.GUEST)
        Assertions.assertEquals(retrivedUser.status, Status.OK)
    }

    @Test
    @Transactional
    fun userRepositoryFindByEmailExcludesExpired() {
        val user = TriftUser()
        user.status = Status.EXPIRED
        user.role = Role.GUEST
        user.authority = Authority.GUEST
        user.userEmail = "aaa@aaa.com"
        userRepository.save(user)


        Assertions.assertNull(userRepository.findByUserEmail("aaa@aaa.com"))

    }
}