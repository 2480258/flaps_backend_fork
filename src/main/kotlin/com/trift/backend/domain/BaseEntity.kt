package com.trift.backend.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity {

    @CreatedDate
    var createdDate: LocalDateTime? = null

    @CreatedBy
    var createdBy: Long? = null

    @LastModifiedDate
    var updatedDate: LocalDateTime? = null

    @LastModifiedBy
    var updatedBy: Long? = null
}
