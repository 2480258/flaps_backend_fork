package com.trift.backend.domain

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.util.*



@Entity
@Table(indexes = [Index(name = "i_owner_id", columnList = "userId")])
class Project {
    enum class Status {
        OK, EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    var projectId: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userId", nullable = false)
    var owner: TriftUser? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="countryId", nullable = false)
    var country: Country? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: Status? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var startAt: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var endAt: Date? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false)
    val createDate: Date? = null

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    val updateDate: Date? = null
}