package com.trift.backend.config

import com.trift.backend.service.AuthService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import java.util.*

@Configuration
class UserAuditorAware(
    val authService: AuthService
) {

    @Bean
    fun auditorAware() = AuditorAware {
        val id = authService.getLoginUser().getUserIdOrNull()
        Optional.ofNullable(id)
    }
}