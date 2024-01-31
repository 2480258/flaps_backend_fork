package com.trift.backend.domain

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.security.InvalidParameterException

class ProjectCopyMask constructor(private val dateMap: Map<Int, Int>) { //Dest Project -> Original Project
    fun findMappedNthDay(nthDayOfDest: Int) : Int? {
        return dateMap[nthDayOfDest]
    }

    fun getMaxDestProjectReference(): Int {
        return dateMap.keys.max()
    }

    fun getMaxSourceProjectReference(): Int {
        return dateMap.values.max()
    }
}