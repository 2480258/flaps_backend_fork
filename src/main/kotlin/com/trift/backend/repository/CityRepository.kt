package com.trift.backend.repository

import com.trift.backend.domain.*
import com.trift.backend.domain.CityInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface CityRepository : CityRepositoryCustom, JpaRepository<City, Long> {
}

interface CityRepositoryCustom {
    fun getCityWithPlaceCount(): List<CityInfo>
}

class CityRepositoryCustomImpl : CityRepositoryCustom, QuerydslRepositorySupport(City::class.java) {
    @Transactional(propagation = Propagation.MANDATORY)
    override fun getCityWithPlaceCount(): List<CityInfo> {
        val qPlace = QPlace.place
        val qCity = QCity.city

        val result = from(qCity)
            .select(
                QCityInfo(
                    qCity.name,
                    qCity.thumbnail,
                    qCity.cityId,
                    qCity.longitude,
                    qCity.latitude,
                    qCity.isAvailable,
                    qCity.country.countryId
                )
            )
            .fetch()

        return result
    }
}