package com.trift.backend.e2e

import com.trift.backend.service.ProjectInfo
import io.restassured.module.kotlin.extensions.Given
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class NearbySearchTest {

    lateinit var adminAccessToken: String
    var cityId: Int = 0
    var countryId: Int = 0
    lateinit var project: ProjectInfo


    @Test
    fun `01_주변 장소 검색`() {
        adminAccessToken = E2ETestUtil.generateAdmin()
        countryId = E2ETestUtil.createCountry(adminAccessToken)
        cityId = E2ETestUtil.createCity(adminAccessToken, countryId)
        project = E2ETestUtil.createProject("name", adminAccessToken, countryId)
        val target = E2ETestUtil.createPlaceRequest("빱 띕", adminAccessToken, countryId, cityId, latitude = 34.6872571, longitude = 135.5258546)
        val matchedWithPositive = E2ETestUtil.createPlaceRequest("ab", adminAccessToken, countryId, cityId, latitude = 34.6864797, longitude = 135.5262114)
        val matchedWithNegative = E2ETestUtil.createPlaceRequest("ab", adminAccessToken, countryId, cityId, latitude = 34.6910576, longitude = 135.5206556)
        val notMacthed1 = E2ETestUtil.createPlaceRequest("aabc", adminAccessToken, countryId, cityId, latitude = 34.6960576, longitude = 135.5126556)
        val notMatched2 = E2ETestUtil.createPlaceRequest("aabc", adminAccessToken, countryId, cityId, latitude = -37.006, longitude = -140.007)

        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place/nearby?placeId=$target&projectId=${project.projectId}")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(2))
            body("data[0].name", CoreMatchers.equalTo("ab"))
            body("data[1].name", CoreMatchers.equalTo("ab"))
        }
    }
}