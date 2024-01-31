package com.trift.backend.service

import com.trift.backend.domain.City
import com.trift.backend.domain.CityInfo
import com.trift.backend.repository.CityRepository
import com.trift.backend.repository.CountryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface CityService {

    fun createCity(name: String, thumbnail: String, longitude: Double, latitude: Double, isAvailable: Boolean, country: Long) : CityInfo

    fun listCityWithPlaces() : List<CityInfo>
}

@Component
class CityServiceImpl : CityService {

    @Autowired
    private lateinit var cityRepository: CityRepository

    @Autowired
    private lateinit var countryRepository: CountryRepository

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun createCity(name: String, thumbnail: String, longitude: Double, latitude: Double, isAvailable: Boolean, country: Long) : CityInfo{
        val city = City().apply {
            this.name = name
            this.thumbnail = thumbnail
            this.longitude = longitude
            this.latitude = latitude
            this.isAvailable = isAvailable
            this.country = countryRepository.findById(country).orElseThrow { throw ResponseStatusException(HttpStatus.NOT_FOUND, "해당하는 국가를 찾을 수 없음") }
        }

        return CityInfo.from(cityRepository.saveAndFlush(city))
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun listCityWithPlaces(): List<CityInfo> {
        return cityRepository.getCityWithPlaceCount()
    }
}