package com.trift.backend.e2e

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.geojson.Feature
import org.geojson.LineString
import org.geojson.LngLatAlt
import org.junit.jupiter.api.*
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.test.annotation.Rollback

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.MethodName::class)
@Rollback
class DirectionTest {
    @Autowired
    @Qualifier("geoJsonCacheManager")
    lateinit var cacheManager: CacheManager

    lateinit var adminAccessToken: String
    var cityId: Int = 0
    var countryId: Int = 0
    var weirdPlaceId: Long = 0
    var place1Id: Long = 0
    var place2Id: Long = 0
    var weirdPlaceLngLat = LngLatAlt(158.1953, 38.7087)
    var place1LngLat = LngLatAlt(8.6878, 49.4203)
    var place2LngLat = LngLatAlt(8.6814, 49.4146)

    @BeforeAll
    fun beforeAll() {
        adminAccessToken = E2ETestUtil.generateAdmin()
        countryId = E2ETestUtil.createCountry(adminAccessToken)
        cityId = E2ETestUtil.createCity(adminAccessToken, countryId)
        weirdPlaceId = E2ETestUtil.createPlaceRequest(
            name = "pacificOcean",
            token = adminAccessToken,
            countryId = countryId,
            cityId = cityId,
            longitude = weirdPlaceLngLat.longitude,
            latitude = weirdPlaceLngLat.latitude
        )
        place1Id = E2ETestUtil.createPlaceRequest(
            name = "place1",
            token = adminAccessToken,
            countryId = countryId,
            cityId = cityId,
            longitude = place1LngLat.longitude,
            latitude = place1LngLat.latitude
        )
        place2Id = E2ETestUtil.createPlaceRequest(
            name = "place2",
            token = adminAccessToken,
            countryId = countryId,
            cityId = cityId,
            longitude = place2LngLat.longitude,
            latitude = place2LngLat.latitude
        )
    }

    @Test
    fun `01_경로_찾기가_가능한_경우_올바른_경로_반환`() {
        val features = Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/direction?startPlaceId=${place1Id}&endPlaceId=${place2Id}")
        } Then {
            statusCode(200)
        } Extract {
            body().jsonPath().getObject("data", Feature::class.java)
        }

        val lineString = features.geometry as LineString

        assertTrue(lineString.coordinates.size > 2)
        assertEquals(place1LngLat, lineString.coordinates.first())
        assertEquals(place2LngLat, lineString.coordinates.last())
    }

    @Test
    fun `02_경로_찾기가_가능한_경우_캐시_적용`() {
        val response1 = Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/direction?startPlaceId=${place2Id}&endPlaceId=${place1Id}")
        } Then {
            statusCode(200)
        } Extract {
            body().jsonPath().getObject("data", Feature::class.java)
        }

        val cache = cacheManager.getCache("direction")
        assert(cache != null)

        val cachedFeature = cache!!.get(
            directionCacheKey(
                startPlaceId = place2Id,
                endPlaceId = place1Id
            ),
            Feature::class.java
        )
        assert(cachedFeature != null)

        val lineString1 = response1.geometry as LineString
        val lineString2 = cachedFeature!!.geometry as LineString

        assertEquals(lineString1.coordinates.size, lineString2.coordinates.size)
        assertEquals(lineString1.coordinates.first(), lineString2.coordinates.first())
        assertEquals(lineString1.coordinates.last(), lineString2.coordinates.last())
    }

    @Test
    fun `03_경로_찾기가_불가능한_경우_직선경로_반환`() {
        val features = Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/direction?startPlaceId=${weirdPlaceId}&endPlaceId=${place2Id}")
        } Then {
            statusCode(200)
        } Extract {
            body().jsonPath().getObject("data", Feature::class.java)
        }

        val lineString = features.geometry as LineString

        assertTrue(lineString.coordinates.size == 2)
        assertEquals(weirdPlaceLngLat, lineString.coordinates.first())
        assertEquals(place2LngLat, lineString.coordinates.last())
    }

    @Test
    fun `04_경로_찾기가_불가능한_경우_캐시_적용`() {
        val response1 = Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/direction?startPlaceId=${place1Id}&endPlaceId=${weirdPlaceId}")
        } Then {
            statusCode(200)
        } Extract {
            body().jsonPath().getObject("data", Feature::class.java)
        }

        val cache = cacheManager.getCache("direction")
        assert(cache != null)

        val cachedFeature = cache!!.get(
            directionCacheKey(
                startPlaceId = place1Id,
                endPlaceId = weirdPlaceId
            ), Feature::class.java
        )
        assert(cachedFeature != null)

        val lineString1 = response1.geometry as LineString
        val lineString2 = cachedFeature!!.geometry as LineString

        assertEquals(lineString1.coordinates, lineString2.coordinates)
    }

    @Test
    fun `05_존재하지_않는_여행지_경로_검색_시_404_반환`() {
        val response = Given {
            header("Authorization", "Bearer $adminAccessToken")
            header("Content-Type", "application/x-www-form-url-encoded")
        } When {
            get("/api/v1/direction?startPlaceId=${weirdPlaceId}&endPlaceId=${-1}")
        } Then {
            statusCode(404)
        }
    }

    fun directionCacheKey(startPlaceId: Long, endPlaceId: Long, profile: String = "driving-car"): String {
        return "Dpf${profile}sId${startPlaceId}eId${endPlaceId}"
    }
}