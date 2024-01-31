package com.trift.backend.service

import com.trift.backend.domain.City
import com.trift.backend.domain.PageableData
import com.trift.backend.repository.CityRepository
import com.trift.backend.repository.CountryRepository
import com.trift.backend.repository.PlaceRepository
import com.trift.backend.repository.PlaceSearch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

interface PlaceCacheService {
    fun getPlaceIdFromSearch(
        searchTerm: String?,
        category: String?,
        cityId: Long?,
        countryId: Long,
        pageOffset: Long,
        pageSize: Int
    ) : PageableData<Long>
}

@Service
class PlaceCacheServiceImpl : PlaceCacheService {
    @Autowired
    private lateinit var placeRepository: PlaceRepository

    @Autowired
    private lateinit var cityRepository: CityRepository

    @Autowired
    private lateinit var countryRepository: CountryRepository

    @Autowired
    private lateinit var projectService: ProjectService

    @Autowired
    private lateinit var preprocessService: SearchPreprocessService

    private val min_SearchTerm_Length = 2
    private val max_SearchTerm_Length = 15
    private fun createPlaceSearch(searchTerm: String?, category: String?, city: City?): PlaceSearch {

        return PlaceSearch(if (searchTerm != null) preprocessService.getHString(searchTerm) else null, category, city)
    }

    private fun validateSearchRequest(search: String) {
        if (search.length != null) {
            if ((search.length < min_SearchTerm_Length) or (search.length > max_SearchTerm_Length)) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "검색어가 너무 짧거나 깁니다. ${search.length} 자")
            }
        }
    }

    @Cacheable(
        cacheNames = arrayOf("placelistWithSearchTerm"),
        key = "'s' + #p0 + 'c' + #p1 + 't' + #p2 + 'o' + #p3 + 'po' + #p4 + 'ps' + #p5",
        sync = false,
        cacheManager = "contentCacheManager"
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    override fun getPlaceIdFromSearch(
        searchTerm: String?,
        category: String?,
        cityId: Long?,
        countryId: Long,
        pageOffset: Long,
        pageSize: Int
    ) : PageableData<Long> {
        val city = if (cityId != null) cityRepository.findById(cityId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "해당하는 도시를 찾을 수 없음") } else null

        if (searchTerm != null) validateSearchRequest(searchTerm)

        val search = createPlaceSearch(searchTerm, category, city)

        val placeId = placeRepository.getPlaceIdFromFullSearch(pageOffset, pageSize, search, countryId)

        return placeId
    }
}