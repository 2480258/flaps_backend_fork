package com.trift.backend.web.controller

import com.trift.backend.web.dto.CommonResponseWithoutData
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException


@RestControllerAdvice
class ResponseAdvisor {
    private val loggerFactory = LoggerFactory.getLogger(ResponseAdvisor::class.java)

    @ExceptionHandler(ResponseStatusException::class)
    fun responseStatusException(e: ResponseStatusException): ResponseEntity<*>? {


        val entity = ResponseEntity.status(e.statusCode)
            .body(CommonResponseWithoutData(e.statusCode.value(), e.message, 0))


        loggerFactory.error("${e.statusCode}: ${e.message} - ${e.stackTraceToString()}")
        return entity
    }

    @ExceptionHandler(Exception::class)
    fun etcException(e: Exception): ResponseEntity<*>? {
        val entity = ResponseEntity.status(500)
            .body(CommonResponseWithoutData(500, e.stackTraceToString(), 0))
        loggerFactory.error("${e.message} - ${e.stackTraceToString()}")
        return entity
    }
}
