package com.trift.backend.e2e

import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class GraphTest {
    lateinit var adminAccessToken1: String
    lateinit var adminAccessToken2: String

    @BeforeAll
    fun beforeAll() {
        adminAccessToken1 = E2ETestUtil.generateAdmin()
        adminAccessToken2 = E2ETestUtil.generateAdmin()
    }

    @Test
    fun `01_프로젝트_생성시_그래프_생성_후_200_반환`() {
        val countryId = E2ETestUtil.createCountry(adminAccessToken1)
        E2ETestUtil.createCity(adminAccessToken1, countryId)
        val project = E2ETestUtil.createProject("name", adminAccessToken1, countryId)

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${project.projectId}&nthDay=0")
        } Then {
            statusCode(200)
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${project.projectId}&nthDay=1")
        } Then {
            statusCode(200)
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${project.projectId}&nthDay=2")
        } Then {
            statusCode(200)
        }
    }

    @Test
    fun `02_다른_유저의_그래프_조회_시_403_반환`() {
        val countryId = E2ETestUtil.createCountry(adminAccessToken1)
        E2ETestUtil.createCity(adminAccessToken1, countryId)
        val project = E2ETestUtil.createProject("name", adminAccessToken1, countryId)

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${project.projectId}&nthDay=0")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `03_없는_그래프_조회_시_404_반환`() {
        val countryId = E2ETestUtil.createCountry(adminAccessToken1)
        E2ETestUtil.createCity(adminAccessToken1, countryId)
        val project = E2ETestUtil.createProject("name", adminAccessToken1, countryId)

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=12345&nthDay=0")
        } Then {
            statusCode(404)
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${project.projectId}&nthDay=12345")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `04_그래프_데이터_변경_후_204_반환`() {
        val countryId = E2ETestUtil.createCountry(adminAccessToken1)
        E2ETestUtil.createCity(adminAccessToken1, countryId)
        val project = E2ETestUtil.createProject("name", adminAccessToken1, countryId)

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
            body(dailyGraphPayload)
        } When {
            post("/api/v1/graph?projectId=${project.projectId}&nthDay=0")
        } Then {
            statusCode(204)
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${project.projectId}&nthDay=0")
        } Then {
            statusCode(200)
            body("data.graphPlaceNodes", Matchers.hasSize<Any>(4))
            body("data.graphPlaceEdges", Matchers.hasSize<Any>(3))
            body("data.nthDay", CoreMatchers.equalTo(0))
        }
    }

    @Test
    fun `05_다른_유저의_그래프_변경_시_403_반환`() {
        val countryId = E2ETestUtil.createCountry(adminAccessToken1)
        E2ETestUtil.createCity(adminAccessToken1, countryId)
        val project = E2ETestUtil.createProject("name", adminAccessToken1, countryId)

        Given {
            header("Authorization", "Bearer $adminAccessToken2")
            header("Content-Type", "application/json")
            body(dailyGraphPayload)
        } When {
            post("/api/v1/graph?projectId=${project.projectId}&nthDay=0")
        } Then {
            statusCode(403)
        }
    }

    @Test
    fun `06_없는_그래프_변경_시_404_반환`() {
        val countryId = E2ETestUtil.createCountry(adminAccessToken1)
        E2ETestUtil.createCity(adminAccessToken1, countryId)
        val project = E2ETestUtil.createProject("name", adminAccessToken1, countryId)

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
            body(dailyGraphPayload)
        } When {
            post("/api/v1/graph?projectId=12345&nthDay=0")
        } Then {
            statusCode(404)
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
            body(dailyGraphPayload)
        } When {
            post("/api/v1/graph?projectId=${project.projectId}&nthDay=12345")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `07_프로젝트_삭제_후_그래프_조회_시_404_반환`() {
        val countryId = E2ETestUtil.createCountry(adminAccessToken1)
        E2ETestUtil.createCity(adminAccessToken1, countryId)
        val project = E2ETestUtil.createProject("name", adminAccessToken1, countryId)

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${project.projectId}&nthDay=0")
        } Then {
            statusCode(200)
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/json")
            body("{\"projectId\" : ${project.projectId} }")
        } When {
            delete("/api/v1/project")
        } Then {
            statusCode(201)
        }

        Given {
            header("Authorization", "Bearer $adminAccessToken1")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/graph?projectId=${project.projectId}&nthDay=0")
        } Then {
            statusCode(404)
        }
    }

    val dailyGraphPayload = """
        {
        "nthday": 0,
        "graphPlaceNodes": [
            {
                "id": "START",
                "type": "startFinishNode",
                "position": {
                    "x": 0,
                    "y": 100
                },
                "data": "START",
                "width": 89,
                "height": 44
            },
            {
                "id": "FINISH",
                "type": "startFinishNode",
                "position": {
                    "x": 0,
                    "y": 600
                },
                "data": "FINISH",
                "width": 89,
                "height": 44
            },
            {
                "id": "31086981-b565-435d-b59b-91b1a4e6e348",
                "type": "graphNode",
                "position": {
                    "x": -90,
                    "y": 435
                },
                "data": {
                    "isChecked": false,
                    "memo": "",
                    "placeId": 1
                },
                "dragging": true,
                "selected": true,
                "width": 270,
                "height": 130,
                "positionAbsolute": {
                    "x": -90,
                    "y": 435
                }
            },
            {
                "id": "155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "type": "graphNode",
                "position": {
                    "x": -135,
                    "y": 225
                },
                "data": {
                    "isChecked": false,
                    "memo": "",
                    "placeId": 1
                },
                "dragging": true,
                "selected": false,
                "width": 270,
                "height": 130,
                "positionAbsolute": {
                    "x": -135,
                    "y": 225
                }
            }
        ],
        "graphPlaceEdges": [
            {
                "id": "proximity-155e0e25-49b6-4806-a44c-ed9f85762fd1-START",
                "source": "START",
                "target": "155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "style": {
                    "strokeDasharray": 5
                },
                "type": "straight"
            },
            {
                "id": "proximity-31086981-b565-435d-b59b-91b1a4e6e348-155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "source": "155e0e25-49b6-4806-a44c-ed9f85762fd1",
                "target": "31086981-b565-435d-b59b-91b1a4e6e348",
                "style": {
                    "strokeDasharray": 5
                },
                "type": "straight"
            },
            {
                "id": "proximity-31086981-b565-435d-b59b-91b1a4e6e348-FINISH",
                "source": "31086981-b565-435d-b59b-91b1a4e6e348",
                "target": "FINISH",
                "style": {
                    "strokeDasharray": 5
                },
                "type": "straight"
            }
        ]
    }
    """.trimIndent()
}