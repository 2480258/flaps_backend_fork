package com.trift.backend.domain

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.util.*


enum class Role {
    ADMIN, MEMBER, GUEST
}

enum class Status {
    OK, EXPIRED
}

enum class Authority {
    GUEST, NAVER
}

@Entity
@Table(indexes = [Index(name = "i_user_email", columnList = "userEmail")])
class TriftUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    var userId: Long? = null

    // Email이 아닌 Naver Login API의 동일인 식별 정보(nid) 사용
    @Column(nullable = true, unique = true)
    var userEmail: String? = null

    @Column(nullable = true)
    var refreshToken: String? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: Status? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role: Role? = null

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var authority: Authority? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false, nullable = false)
    val createDate: Date? = null

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    val updateDate: Date? = null
}