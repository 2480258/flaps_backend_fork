package com.trift.backend.service

import com.trift.backend.domain.OptimizedRoute
import com.trift.backend.domain.OptimizedRouteItem
import com.trift.backend.repository.PlaceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

enum class GoogleRouteMode {
    DRIVE, WALK
}

interface GoogleRouteQOSService {
    fun sendRequestWithQOS(start: Long, end: Long, intermediate: List<Long>, mode: GoogleRouteMode): OptimizedRoute

    fun leftToken(): Long
}

@Component
class GoogleRouteQOSServiceImpl : GoogleRouteQOSService {
    @Autowired
    lateinit var googleRouteAPIService: GoogleRouteAPIService

    @Autowired
    lateinit var qosService: QOSService

    @Autowired
    lateinit var authService: AuthService

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun sendRequestWithQOS(
        start: Long,
        end: Long,
        intermediate: List<Long>,
        mode: GoogleRouteMode
    ): OptimizedRoute {
        if (intermediate.size < 2) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "3개 이하의 장소로 경로를 만들 수 없음")
        }

        return qosService.consumesTokenWith(authService.getLoginUser()) {
            googleRouteAPIService.sendRequest(start, end, intermediate, mode)
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun leftToken(): Long {
        return qosService.getLeftToken(authService.getLoginUser())
    }
}

interface GoogleRouteAPIService {
    fun sendRequest(start: Long, end: Long, intermediate: List<Long>, mode: GoogleRouteMode): OptimizedRoute
}

data class LatLng(
    val latitude: Double,
    val longitude: Double
)

data class StartLocation(
    val latLng: LatLng
)

data class EndLocation(
    val latLng: LatLng
)

data class Legs(
    val distanceMeters: Int,
    val duration: String,
    val startLocation: StartLocation,
    val endLocation: EndLocation,
    val localizedValues: LocalizedValues
)

data class LocalizedValues(
    val distance: LocalizedDistance,
    val duration: LocalizedDuration,
    val staticDuration: LocalizedStaticDuration
)

data class LocalizedDistance(val text: String)

data class LocalizedDuration(val text: String)

data class LocalizedStaticDuration(val text: String)

data class Routes(
    val legs: List<Legs>,
    val distanceMeters: Int,
    val duration: String,
    val optimizedIntermediateWaypointIndex: List<Int>
)

data class GoogleRouteAPIResponse(
    val routes: List<Routes>
)

data class JsonPlaceId(
    val placeId: String
)

data class GoogleRouteAPIRequest(
    val origin: JsonPlaceId?,
    val destination: JsonPlaceId?,
    val intermediates: List<JsonPlaceId>?,
    val travelMode: String?,
    val optimizeWaypointOrder: String?,
    val languageCode: String?,
    val units: String?
)

@Service
@Profile("!prod & !staging")
class GoogleRouteAPIServiceMock : GoogleRouteAPIService {

    @Autowired
    lateinit var placeRepository: PlaceRepository

    override fun sendRequest(start: Long, end: Long, intermediate: List<Long>, mode: GoogleRouteMode): OptimizedRoute {
        val routeList = mutableListOf<OptimizedRouteItem>()
        val placeList = listOf(start).plus(intermediate).plus(listOf(end))

        for (i in 0 until placeList.size - 1) {
            routeList.add(OptimizedRouteItem(placeList[i], placeList[i + 1], i.toDouble(), i.toLong().toString()))
        }

        return OptimizedRoute(routeList)
    }
}

@Service
@Profile("prod | staging")
class GoogleRouteAPIServiceImpl : GoogleRouteAPIService {

    @Value("\${google.api}")
    lateinit var apiKey: String

    @Autowired
    lateinit var placeRepository: PlaceRepository

    override fun sendRequest(start: Long, end: Long, intermediate: List<Long>, mode: GoogleRouteMode): OptimizedRoute {
        val intermediateIdList = placeRepository.getGooglePlaceIdByPlaceId(intermediate)
        val startPlaceId = placeRepository.getGooglePlaceIdByPlaceId(listOf(start)).single().googlePlaceId
        val endPlaceId = placeRepository.getGooglePlaceIdByPlaceId(listOf(end)).single().googlePlaceId

        val mode = if (mode == GoogleRouteMode.DRIVE) "DRIVE" else "WALK"

        val request = GoogleRouteAPIRequest(
            JsonPlaceId(startPlaceId),
            JsonPlaceId(endPlaceId),
            intermediateIdList.map { x -> JsonPlaceId(x.googlePlaceId) },
            mode,
            "true",
            "ko",
            "METRIC"
        )

        val headers: MultiValueMap<String, String> = LinkedMultiValueMap()
        headers.add("Content-Type", "application/json")
        headers.add("X-Goog-Api-Key", apiKey)
        headers.add(
            "X-Goog-FieldMask",
            "routes.localizedValues,routes.legs.localizedValues,routes.duration,routes.distanceMeters,routes.legs.distanceMeters,routes.legs.duration,routes.legs.startLocation,routes.legs.endLocation,routes.optimized_intermediate_waypoint_index"
        )

        val entity: ResponseEntity<GoogleRouteAPIResponse> = RestTemplate().exchange(
            "https://routes.googleapis.com/directions/v2:computeRoutes",
            HttpMethod.POST,
            HttpEntity<GoogleRouteAPIRequest>(request, headers),
            GoogleRouteAPIResponse::class.java
        )

        if (entity.body == null) {
            throw ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Google API 통신 오류, Status Code: ${entity.statusCode}"
            )
        }

        val sortedIntermediate =
            intermediate.zip(entity.body!!.routes[0].optimizedIntermediateWaypointIndex).sortedBy { it.second }
                .map { it.first }
        val entirePath = listOf(start).plus(sortedIntermediate).plus(listOf(end))

        val routeItems = mutableListOf<OptimizedRouteItem>()
        for (i in 0 until entirePath.size - 1) {
            routeItems.add(
                OptimizedRouteItem(
                    entirePath[i],
                    entirePath[i + 1],
                    entity.body!!.routes[0].legs[i].distanceMeters.toDouble(),
                    entity.body!!.routes[0].legs[i].localizedValues.duration.text
                )
            )
        }

        return OptimizedRoute(routeItems)
    }
}