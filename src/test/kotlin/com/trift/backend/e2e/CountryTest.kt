package com.trift.backend.e2e

import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CountryTest {
    @Test
    fun getCountryList() {
        val admin = E2ETestUtil.generateAdmin()
        E2ETestUtil.createCountry(admin)

        When {
            get("/api/v1/country")
        } Then {
            statusCode(200)
            body("data", Matchers.hasSize<Any>(1))
        }
    }
}