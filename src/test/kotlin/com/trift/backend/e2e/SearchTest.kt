package com.trift.backend.e2e

import com.trift.backend.domain.PageableData
import com.trift.backend.service.ProjectInfo
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SearchTest {
    lateinit var adminAccessToken: String
    var cityId: Int = 0
    var countryId: Int = 0
    lateinit var project: ProjectInfo

    @Autowired
    lateinit var cacheManager: CacheManager

    @Test
    fun `01_한글_문자열_검색`() {
        adminAccessToken = E2ETestUtil.generateAdmin()
        countryId = E2ETestUtil.createCountry(adminAccessToken)
        cityId = E2ETestUtil.createCity(adminAccessToken, countryId)
        project = E2ETestUtil.createProject("name", adminAccessToken, countryId)
        E2ETestUtil.createPlaceRequest("빱 띕", adminAccessToken, countryId, cityId)
        E2ETestUtil.createPlaceRequest("ab", adminAccessToken, countryId, cityId)

        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?searchTerm=빱띕&category=queried_category&projectId=1&page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(1))
            body("data.content[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data.content[0].info.name", CoreMatchers.equalTo("빱 띕"))
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
    fun `02_초성으로 문자열 검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?searchTerm=ㅃㄸ&category=queried_category&projectId=1&page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(1))
            body("data.content[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data.content[0].info.name", CoreMatchers.equalTo("빱 띕"))
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
    fun `02_완성되지 않은 문자열로 검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?searchTerm=빱뜨&category=queried_category&projectId=1&page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(1))
            body("data.content[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data.content[0].info.name", CoreMatchers.equalTo("빱 띕"))
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
    fun `03_대문자_영어 문자열로 검색`() {
        Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/json")
        } When {
            get("/api/v1/place?searchTerm=Ab&category=queried_category&projectId=1&page=0&size=10")
        } Then {
            statusCode(200)
            body("data.content", Matchers.hasSize<Any>(1))
            body("data.content[0].info.thumbnail", CoreMatchers.equalTo("photo"))
            body("data.content[0].info.name", CoreMatchers.equalTo("ab"))
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
    fun `04_캐시_체크`() {
        val cache = cacheManager.getCache("placelistWithSearchTerm")
        assert(cache != null)

        val ret = cache!!.get("sAbcqueried_categorytnullo1po0ps10")!!.get() as PageableData<Long>
        assert(ret.total > 0)
    }
}