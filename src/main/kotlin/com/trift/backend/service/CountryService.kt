package com.trift.backend.service

import com.trift.backend.domain.Country
import com.trift.backend.domain.CountryInfo
import com.trift.backend.repository.CountryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

interface CountryService {
    fun createCountry(name: String, thumbnail: String, isAvailable: Boolean, latitude: Double, longitude: Double) : CountryInfo

    fun listCountry(): List<CountryInfo>
}

@Component
class CountryServiceImpl : CountryService {

    @Autowired
    private lateinit var countryRepository: CountryRepository

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun createCountry(name: String, thumbnail: String, isAvailable: Boolean, latitude: Double, longitude: Double) : CountryInfo {
        val country = Country().apply {
            this.name = name
            this.thumbnail = thumbnail
            this.isAvailable = isAvailable
            this.latitude = latitude
            this.longitude = longitude
        }

        return CountryInfo.from(countryRepository.saveAndFlush(country))
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun listCountry(): List<CountryInfo> {
        return countryRepository.getCountryList()
    }
}