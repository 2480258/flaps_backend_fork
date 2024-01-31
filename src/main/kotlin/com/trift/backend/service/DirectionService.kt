package com.trift.backend.service

import com.trift.backend.domain.Place
import com.trift.backend.repository.PlaceRepository
import org.geojson.Feature
import org.geojson.FeatureCollection
import org.geojson.LineString
import org.geojson.LngLatAlt
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

interface DirectionService {
    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun getDirection(startPlaceId: Long, endPlaceId: Long, profile: String): Feature;
}

@Service
class DirectionServiceImpl(
    val placeRepository: PlaceRepository
): DirectionService {

    val apiUrl = "https://api.openrouteservice.org/v2/directions"

    @Value("\${openrouteservice.api-key}")
    lateinit var apiKey: String

    @Cacheable(
        cacheNames = ["direction"],
        key = "'D' + 'pf' + #profile + 'sId' + #startPlaceId + 'eId' + #endPlaceId",
        sync = false,
        cacheManager = "geoJsonCacheManager",
        unless = "{#result == null || #result.getProperty(\"cacheCondition\") == false}",
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun getDirection(startPlaceId: Long, endPlaceId: Long, profile: String): Feature {
        val startPlace = placeRepository.findById(startPlaceId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Start Place Id가 유효하지 않습니다")
        }
        val endPlace = placeRepository.findById(endPlaceId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "End Place ID가 유효하지 않습니다")
        }
        val straightDirection = getStraightDirection(
            startPlace.outscrapper_latitude!!,
            startPlace.outscrapper_longitude!!,
            endPlace.outscrapper_latitude!!,
            endPlace.outscrapper_longitude!!
        )
        val feature = try {
            val restTemplate = RestTemplate();
//            println("API CALLED")
            val resp = restTemplate.getForEntity(
                "${apiUrl}/${profile}" +
                        "?api_key=${apiKey}" +
                        "&start=${startPlace.outscrapper_longitude},${startPlace.outscrapper_latitude}" +
                        "&end=${endPlace.outscrapper_longitude},${endPlace.outscrapper_latitude}",
                FeatureCollection::class.java
            )
            val direction = wrapDirectionWithPlaces(
                feature = resp.body!!.features[0],
                startLatitude = startPlace.outscrapper_latitude!!,
                startLongitude = startPlace.outscrapper_longitude!!,
                endLatitude = endPlace.outscrapper_latitude!!,
                endLongitude = endPlace.outscrapper_longitude!!
            )
            setProperty(direction, true, startPlace, endPlace)

        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.BAD_REQUEST || e.statusCode == HttpStatus.NOT_FOUND) {
                setProperty(straightDirection, true, startPlace, endPlace)
            } else {
                setProperty(straightDirection, false, startPlace, endPlace)
            }
        } catch (e: Exception) {
            setProperty(straightDirection, false, startPlace, endPlace)
        }

        return feature;
    }

    fun getStraightDirection(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Feature {
        val feature = Feature()
        feature.geometry = LineString(LngLatAlt(startLongitude, startLatitude), LngLatAlt(endLongitude, endLatitude))
        return feature
    }

    fun wrapDirectionWithPlaces(
        feature: Feature,
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Feature {
        val lineString = feature.geometry as LineString
        lineString.coordinates.add(0, LngLatAlt(startLongitude, startLatitude))
        lineString.coordinates.add(LngLatAlt(endLongitude, endLatitude))
        return feature;
    }

    fun setProperty(feature: Feature, cacheCondition: Boolean, startPlace: Place, endPlace: Place): Feature {
        feature.setProperty("cacheCondition", cacheCondition)
        feature.setProperty("startPlaceId", startPlace.placeId)
        feature.setProperty("endPlaceId", endPlace.placeId)
        feature.setProperty("startPlaceName", startPlace.outscrapper_name)
        feature.setProperty("endPlaceName", endPlace.outscrapper_name)
        return feature
    }
}