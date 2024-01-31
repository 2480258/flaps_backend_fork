package com.trift.backend.repository

import com.trift.backend.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

interface CountryRepository : CountryRepositoryCustom, JpaRepository<Country, Long> {
}


interface CountryRepositoryCustom {
    fun getCountryList(): List<CountryInfo>
}


class CountryRepositoryCustomImpl : CountryRepositoryCustom, QuerydslRepositorySupport(Country::class.java) {
    @Transactional(propagation = Propagation.MANDATORY)
    override fun getCountryList(): List<CountryInfo> {
        val qCountry = QCountry.country

        val result = from(qCountry)
            .select(
                QCountryInfo(
                    qCountry.name,
                    qCountry.thumbnail,
                    qCountry.countryId,
                    qCountry.isAvailable,
                    qCountry.latitude,
                    qCountry.longitude
                )
            )
            .fetch()

        return result
    }
}