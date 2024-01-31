package com.trift.backend.e2e

import com.github.fppt.jedismock.RedisServer
import com.github.fppt.jedismock.operations.server.MockExecutor
import com.github.fppt.jedismock.server.RedisCommandInterceptor
import com.github.fppt.jedismock.server.ServiceOptions
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class RouteTest {
    lateinit var adminAccessToken1: String
    lateinit var adminAccessToken2: String
    lateinit var adminAccessToken3: String

    var country = 0
    var city = 0
    var projectId: Int = 0
    var token: String? = null
    var place1 = 0L
    var place2 = 0L
    var place3 = 0L
    var place4 = 0L

    @Test
    fun `01_calculateRouteTestWithGuest`() {
        adminAccessToken1 = E2ETestUtil.generateAdmin()
        adminAccessToken2 = E2ETestUtil.generateAdmin()
        adminAccessToken3 = E2ETestUtil.generateAdmin()
        country = E2ETestUtil.createCountry(adminAccessToken1)
        city = E2ETestUtil.createCity(adminAccessToken1, country)
        place1 = E2ETestUtil.createPlaceRequest("a", adminAccessToken1, country, city)
        place2 = E2ETestUtil.createPlaceRequest("b", adminAccessToken1, country, city)
        place3 = E2ETestUtil.createPlaceRequest("c", adminAccessToken1, country, city)
        place4 = E2ETestUtil.createPlaceRequest("d", adminAccessToken1, country, city)

        val guestToken = E2ETestUtil.generateGuest()
        Given {
            header("Authorization", "Bearer $guestToken")
            header("Content-Type", "application/json")
            body(
                """
                {"placeIds":[$place1, $place2, $place3, $place4]}
            """.trimIndent()
            )
        } When {
            post("/api/v1/route/drive")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `02_calculateRouteTestWithAdmin`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
            body(
                """
                {"placeIds":[$place1, $place2, $place3, $place4]}
            """.trimIndent()
            )
        } When {
            post("/api/v1/route/drive")
        } Then {
            statusCode(200)
            body("data.routes", Matchers.hasSize<Any>(3))
            body("data.routes[0].from", CoreMatchers.equalTo(place1.toInt()))
            body("data.routes[0].to", CoreMatchers.equalTo(place2.toInt()))
            body("data.routes[0].distance", CoreMatchers.equalTo<Float>(0.0f))
            body("data.routes[0].time", CoreMatchers.equalTo("0"))
        }
    }

    @Test
    fun `03_getLeftTokenWithAdmin`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/route/left")
        } Then {
            statusCode(200)
            body("data.left", CoreMatchers.equalTo(4))
        }
    }

    @Test
    fun `04_calculateRouteTestWithGuest`() {
        for (i in 0 until 5) {
            Given {
                header("Authorization", "Bearer $adminAccessToken2")
                header("Content-Type", "application/json")
                body(
                    """
                {"placeIds":[$place1, $place2, $place3, $place4]}
            """.trimIndent()
                )
            } When {
                post("/api/v1/route/drive")
            } Then {
                statusCode(200)
                body("data.routes", Matchers.hasSize<Any>(3))
                body("data.routes[0].from", CoreMatchers.equalTo(place1.toInt()))
                body("data.routes[0].to", CoreMatchers.equalTo(place2.toInt()))
                body("data.routes[0].distance", CoreMatchers.equalTo<Float>(0.0f))
                body("data.routes[0].time", CoreMatchers.equalTo("0"))
            }
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
            body(
                """
                {"placeIds":[$place1, $place2, $place3, $place4]}
            """.trimIndent()
            )
        } When {
            post("/api/v1/route/drive")
        } Then {
            statusCode(429)
        }
    }

    @Test
    fun `05_getLeftTokenWithMaxedOutAdmin`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/route/left")
        } Then {
            statusCode(200)
            body("data.left", CoreMatchers.equalTo(0))
        }
    }

    @Test
    fun `06_calculateImpossibleRouteTestWithAdmin`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken3")
            header("Content-Type", "application/json")
            body(
                """
                {"placeIds":[$place1]}
            """.trimIndent()
            )
        } When {
            post("/api/v1/route/drive")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `07_getLeftTokenWithMaxedOutAdmin`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken3")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/route/left")
        } Then {
            statusCode(200)
            body("data.left", CoreMatchers.equalTo(5))
        }
    }
}