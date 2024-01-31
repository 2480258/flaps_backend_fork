package com.trift.backend.e2e

import com.github.fppt.jedismock.RedisServer
import com.github.fppt.jedismock.server.ServiceOptions
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import java.util.Date


@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
class CityAndPlaceTest {

    lateinit var adminAccessToken: String
    var cityId: Int = 0
    var countryId: Int = 0
    var server: RedisServer? = null
    @BeforeAll
    fun before() {
        server = RedisServer.newRedisServer(6380)
        server!!.start()
    }

    @AfterAll
    fun after() {
        server?.stop()
    }

    @Test
    fun `01_도시_생성_후_201_반환`() {
        adminAccessToken = E2ETestUtil.generateAdmin()
        countryId = E2ETestUtil.createCountry(adminAccessToken)
        cityId = E2ETestUtil.createCity(adminAccessToken, countryId)
    }

    @Test
    fun `02_도시에_장소_생성_후_201_반환`() {
        E2ETestUtil.createPlaceRequest("name", adminAccessToken, countryId, cityId)
        E2ETestUtil.createPlaceRequest("another", adminAccessToken, countryId, cityId)
    }

    @Test
    fun `03_도시의_목록_조회_후_200_반환`() {

        When {
            get("/api/v1/city")
        } Then {
            statusCode(200)
            body("data[0].name", CoreMatchers.equalTo("a"))
            body("data[0].thumbnail", CoreMatchers.equalTo("b"))
            body("data[0].longitude", CoreMatchers.equalTo(1.2f))
            body("data[0].latitude", CoreMatchers.equalTo(2.3f))
            body("data[0].isAvailable", CoreMatchers.equalTo(true))
            body("data[0].countryId", CoreMatchers.equalTo(countryId))
        }
    }

    @Test
    fun `04_프로젝트_생성_후_201_반환`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
            body(
                """{
  "name": "name",
  "countryId": $countryId,
  "startTimeStamp": 2,
  "endTimeStamp": 3
}"""
            )
        } When {
            post("/api/v1/project")
        } Then {
            statusCode(201)
            body("data.countryId", CoreMatchers.equalTo(countryId))
            body("data.name", CoreMatchers.equalTo("name"))
            body("data.startTimeStamp", CoreMatchers.equalTo(2))
            body("data.endTimeStamp", CoreMatchers.equalTo(3))
            body("data.thumbnail", CoreMatchers.equalTo("b"))
            body("data.creationTimeStamp", CoreMatchers.anything())
            body("data.projectId", CoreMatchers.equalTo(1))
            body("data.country.name", CoreMatchers.equalTo("a"))
            body("data.country.thumbnail", CoreMatchers.equalTo("b"))
            body("data.country.longitude", CoreMatchers.equalTo(1.2f))
            body("data.country.latitude", CoreMatchers.equalTo(2.3f))
            body("data.country.isAvailable", CoreMatchers.equalTo(true))
            body("data.country.countryId", CoreMatchers.equalTo(countryId))
        }
    }

    @Test
    fun `05_단일_프로젝트_조회_후_200_반환`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project/1")
        } Then {
            statusCode(200)
            body("data.name", CoreMatchers.equalTo("name"))
            body("data.countryId", CoreMatchers.equalTo(countryId))
            body("data.startTimeStamp", CoreMatchers.equalTo(2))
            body("data.endTimeStamp", CoreMatchers.equalTo(3))
            body("data.thumbnail", CoreMatchers.equalTo("b"))
            body("data.creationTimeStamp", CoreMatchers.anything())
            body("data.projectId", CoreMatchers.equalTo(1))
        }
    }

    @Test
    fun `06_프로젝트_목록_조회_후_200_반환`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project?page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content[0].name", CoreMatchers.equalTo("name"))
            body("data.content[0].countryId", CoreMatchers.equalTo(countryId))
            body("data.content[0].startTimeStamp", CoreMatchers.equalTo(2))
            body("data.content[0].endTimeStamp", CoreMatchers.equalTo(3))
            body("data.content[0].thumbnail", CoreMatchers.equalTo("b"))
            body("data.content[0].creationTimeStamp", CoreMatchers.anything())
            body("data.content[0].projectId", CoreMatchers.equalTo(1))
        }
    }

    @Test
    fun `07_버킷에_없는_장소_세부사항_조회`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place/detail?projectId=1&placeId=1")
        } Then {
            statusCode(200)
            body("data.placeId", CoreMatchers.equalTo(1))
            body("data.isLiked", CoreMatchers.equalTo(false))
            body("data.info.description", CoreMatchers.equalTo("description"))
            body("data.info.type", CoreMatchers.equalTo("type"))
            body("data.info.latitude", CoreMatchers.equalTo(1.2f))
            body("data.info.longitude", CoreMatchers.equalTo(2.3f))
            body("data.info.rating", CoreMatchers.equalTo(3.4f))
            body("data.info.reviews_per_score", CoreMatchers.anything())
            body("data.info.name", CoreMatchers.equalTo("name"))
            body("data.info.subtypes", CoreMatchers.anything())
            body("data.info.phone", CoreMatchers.equalTo("phone"))
            body("data.info.full_address", CoreMatchers.equalTo("full_address"))
            body("data.info.photo", CoreMatchers.equalTo("photo"))
            body("data.info.range", CoreMatchers.equalTo("range"))
            body("data.info.about", CoreMatchers.anything())
            body("data.info.location_link", CoreMatchers.equalTo("location_link"))
            body("data.info.working_hours", CoreMatchers.anything())
        }
    }

    @Test
    fun `07_버킷에_없는_장소_검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?searchTerm=name&category=queried_category&projectId=1&page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(1))
            body("data.content[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data.content[0].info.name", CoreMatchers.equalTo("name"))
            body("data.content[0].info.description", CoreMatchers.equalTo("description"))
            body("data.content[0].info.type", CoreMatchers.equalTo("type"))
            body("data.content[0].info.latitude", CoreMatchers.equalTo(1.2f))
            body("data.content[0].info.longitude", CoreMatchers.equalTo(2.3f))
            body("data.content[0].info.rating", CoreMatchers.equalTo(3.4f))
            body("data.content[0].info.reviews_per_score", CoreMatchers.anything())
            body("data.content[0].isLiked", CoreMatchers.equalTo(false))
        }
    }

    @Test
    fun `07_SearchTerm_없이_장소_검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?category=queried_category&projectId=1&page=0&size=10&city=$cityId")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(2))
        }
    }

    @Test
    fun `07_너무_짧은_SearchTerm`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?searchTerm=n&projectId=1&page=0&size=10")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `07_너무_긴_SearchTerm`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?searchTerm=1234567890123456&projectId=1&page=0&size=10")
        } Then {
            statusCode(400)
        }
    }


    @Test
    fun `07_Category_없이_장소_검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?searchTerm=name&projectId=1&page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(1))
        }
    }

    @Test
    fun `07_모든_조건_없이_장소_검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?projectId=1&page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(2))
            body("data.content[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data.content[0].info.name", CoreMatchers.equalTo("another"))
            body("data.content[0].info.description", CoreMatchers.equalTo("description"))
            body("data.content[0].info.type", CoreMatchers.equalTo("type"))
            body("data.content[0].info.latitude", CoreMatchers.equalTo(1.2f))
            body("data.content[0].info.longitude", CoreMatchers.equalTo(2.3f))
            body("data.content[0].info.rating", CoreMatchers.equalTo(3.4f))
            body("data.content[0].info.reviews_per_score", CoreMatchers.anything())
        }
    }

    @Test
    fun `08_버킷에_장소_추가`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
            body("{\"projectId\" : 1, \"placeId\": 1 }")
        } When {
            val pp = post("/api/v1/bucket")
            pp.prettyPrint()
            pp
        } Then {
            statusCode(201)
            body("data", Matchers.hasSize<Any>(1))
            body("data[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data[0].info.name", CoreMatchers.equalTo("name"))
            body("data[0].info.description", CoreMatchers.equalTo("description"))
            body("data[0].info.type", CoreMatchers.equalTo("type"))
            body("data[0].info.latitude", CoreMatchers.equalTo(1.2f))
            body("data[0].info.longitude", CoreMatchers.equalTo(2.3f))
            body("data[0].info.rating", CoreMatchers.equalTo(3.4f))
            body("data[0].info.reviews_per_score", CoreMatchers.anything())
            body("data[0].addedTimestamp", CoreMatchers.allOf(Matchers.lessThan(Date().time), Matchers.greaterThan(1698430387520)))
        }
    }


    @Test
    fun `09_버킷에_장소_중복해서_추가`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
            body("{\"projectId\" : 1, \"placeId\": 1 }")
        } When {
            post("/api/v1/bucket")
        } Then {
            statusCode(304)

        }
    }

    @Test
    fun `09_버킷에_있는_장소_세부사항_조회`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place/detail?projectId=1&placeId=1")
        } Then {
            statusCode(200)
            body("data.placeId", CoreMatchers.equalTo(1))
            body("data.isLiked", CoreMatchers.equalTo(true))
            body("data.info.description", CoreMatchers.equalTo("description"))
            body("data.info.type", CoreMatchers.equalTo("type"))
            body("data.info.latitude", CoreMatchers.equalTo(1.2f))
            body("data.info.longitude", CoreMatchers.equalTo(2.3f))
            body("data.info.rating", CoreMatchers.equalTo(3.4f))
            body("data.info.reviews_per_score", CoreMatchers.anything())
            body("data.info.name", CoreMatchers.equalTo("name"))
            body("data.info.subtypes", CoreMatchers.anything())
            body("data.info.phone", CoreMatchers.equalTo("phone"))
            body("data.info.full_address", CoreMatchers.equalTo("full_address"))
            body("data.info.photo", CoreMatchers.equalTo("photo"))
            body("data.info.range", CoreMatchers.equalTo("range"))
            body("data.info.about", CoreMatchers.anything())
            body("data.info.location_link", CoreMatchers.equalTo("location_link"))
            body("data.info.working_hours", CoreMatchers.anything())
        }
    }

    @Test
    fun `09_버킷에_있는_장소_검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            val resp = get("/api/v1/place?searchTerm=name&category=queried_category&projectId=1&page=0&size=10")
            resp.prettyPrint()
            resp
        } Then {
            statusCode(200)
            body("data.content[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data.content[0].info.name", CoreMatchers.equalTo("name"))
            body("data.content[0].info.description", CoreMatchers.equalTo("description"))
            body("data.content[0].info.type", CoreMatchers.equalTo("type"))
            body("data.content[0].info.latitude", CoreMatchers.equalTo(1.2f))
            body("data.content[0].info.longitude", CoreMatchers.equalTo(2.3f))
            body("data.content[0].info.rating", CoreMatchers.equalTo(3.4f))
            body("data.content[0].info.reviews_per_score", CoreMatchers.anything())
            body("data.content[0].isLiked", CoreMatchers.equalTo(true))
        }
    }

    @Test
    fun `09_버킷의_장소_조회`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/bucket?projectId=1")
        } Then {
            statusCode(200)
            body("data", Matchers.hasSize<Any>(1))
            body("data[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data[0].info.name", CoreMatchers.equalTo("name"))
            body("data[0].info.description", CoreMatchers.equalTo("description"))
            body("data[0].info.type", CoreMatchers.equalTo("type"))
            body("data[0].info.latitude", CoreMatchers.equalTo(1.2f))
            body("data[0].info.longitude", CoreMatchers.equalTo(2.3f))
            body("data[0].info.rating", CoreMatchers.equalTo(3.4f))
            body("data[0].info.reviews_per_score", CoreMatchers.anything())
            body("data[0].isLiked", CoreMatchers.equalTo(true))

        }
    }

    @Test
    fun `10_버킷의_장소_삭제`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
            body("{\"projectId\" : 1, \"placeId\": 1 }")
        } When {
            delete("/api/v1/bucket")
        } Then {
            statusCode(201)
            body("data.bucket", Matchers.hasSize<Any>(0))
        }
    }

    @Test
    fun `11_버킷에서_지워진_장소_세부사항_조회`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place/detail?projectId=1&placeId=1")
        } Then {
            statusCode(200)
            body("data.placeId", CoreMatchers.equalTo(1))
            body("data.isLiked", CoreMatchers.equalTo(false))
            body("data.info.description", CoreMatchers.equalTo("description"))
            body("data.info.type", CoreMatchers.equalTo("type"))
            body("data.info.latitude", CoreMatchers.equalTo(1.2f))
            body("data.info.longitude", CoreMatchers.equalTo(2.3f))
            body("data.info.rating", CoreMatchers.equalTo(3.4f))
            body("data.info.reviews_per_score", CoreMatchers.anything())
            body("data.info.name", CoreMatchers.equalTo("name"))
            body("data.info.subtypes", CoreMatchers.anything())
            body("data.info.phone", CoreMatchers.equalTo("phone"))
            body("data.info.full_address", CoreMatchers.equalTo("full_address"))
            body("data.info.photo", CoreMatchers.equalTo("photo"))
            body("data.info.range", CoreMatchers.equalTo("range"))
            body("data.info.about", CoreMatchers.anything())
            body("data.info.location_link", CoreMatchers.equalTo("location_link"))
            body("data.info.working_hours", CoreMatchers.anything())
        }
    }

    @Test
    fun `11_버킷에서_삭제된_장소_검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            val resp = get("/api/v1/place?searchTerm=name&category=queried_category&projectId=1&page=0&size=10")
            resp.prettyPrint()
            resp
        } Then {
            statusCode(200)
            body("data.content[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data.content[0].info.name", CoreMatchers.equalTo("name"))
            body("data.content[0].info.description", CoreMatchers.equalTo("description"))
            body("data.content[0].info.type", CoreMatchers.equalTo("type"))
            body("data.content[0].info.latitude", CoreMatchers.equalTo(1.2f))
            body("data.content[0].info.longitude", CoreMatchers.equalTo(2.3f))
            body("data.content[0].info.rating", CoreMatchers.equalTo(3.4f))
            body("data.content[0].info.reviews_per_score", CoreMatchers.anything())
            body("data.content[0].isLiked", CoreMatchers.equalTo(false))
        }
    }

    @Test
    fun `12_버킷에서_삭제된_장소_다시_추가`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
            body("{\"projectId\" : 1, \"placeId\": 1 }")
        } When {
            post("/api/v1/bucket")
        } Then {
            statusCode(201)
            body("data", Matchers.hasSize<Any>(1))
            body("data[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data[0].info.name", CoreMatchers.equalTo("name"))
            body("data[0].info.description", CoreMatchers.equalTo("description"))
            body("data[0].info.type", CoreMatchers.equalTo("type"))
            body("data[0].info.latitude", CoreMatchers.equalTo(1.2f))
            body("data[0].info.longitude", CoreMatchers.equalTo(2.3f))
            body("data[0].info.rating", CoreMatchers.equalTo(3.4f))
            body("data[0].info.reviews_per_score", CoreMatchers.anything())
            body("data[0].isLiked", CoreMatchers.equalTo(true))
            body("data[0].addedTimestamp", CoreMatchers.allOf(Matchers.lessThan(Date().time), Matchers.greaterThan(1698430387520)))
        }
    }

    @Test
    fun `13_버킷에서_다시_추가된_장소_세부사항_조회`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place/detail?projectId=1&placeId=1")
        } Then {
            statusCode(200)
            body("data.placeId", CoreMatchers.equalTo(1))
            body("data.isLiked", CoreMatchers.equalTo(true))
            body("data.info.description", CoreMatchers.equalTo("description"))
            body("data.info.type", CoreMatchers.equalTo("type"))
            body("data.info.latitude", CoreMatchers.equalTo(1.2f))
            body("data.info.longitude", CoreMatchers.equalTo(2.3f))
            body("data.info.rating", CoreMatchers.equalTo(3.4f))
            body("data.info.reviews_per_score", CoreMatchers.anything())
            body("data.info.name", CoreMatchers.equalTo("name"))
            body("data.info.subtypes", CoreMatchers.anything())
            body("data.info.phone", CoreMatchers.equalTo("phone"))
            body("data.info.full_address", CoreMatchers.equalTo("full_address"))
            body("data.info.photo", CoreMatchers.equalTo("photo"))
            body("data.info.range", CoreMatchers.equalTo("range"))
            body("data.info.about", CoreMatchers.anything())
            body("data.info.location_link", CoreMatchers.equalTo("location_link"))
            body("data.info.working_hours", CoreMatchers.anything())
        }
    }

    @Test
    fun `12_프로젝트_삭제_후_201_반환`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
            body("{\"projectId\" : 1 }")
        } When {
            delete("/api/v1/project")
        } Then {
            statusCode(201)
            body("data.name", CoreMatchers.equalTo("name"))
            body("data.countryId", CoreMatchers.equalTo(countryId))
            body("data.startTimeStamp", CoreMatchers.equalTo(2))
            body("data.endTimeStamp", CoreMatchers.equalTo(3))
            body("data.thumbnail", CoreMatchers.equalTo("b"))
            body("data.creationTimeStamp", CoreMatchers.anything())
            body("data.projectId", CoreMatchers.equalTo(1))
        }
    }

    @Test
    fun `14_삭제된_프로젝트_조회_후_404_반환`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/project/1")
        } Then {
            statusCode(404)
        }
    }

    @Test
    fun `14_프로젝트_목록_조회_후_빈_리스트_200_반환`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            val resp = get("/api/v1/project?page=0&size=10")
            resp.prettyPrint()
            resp
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(0))
        }
    }
}