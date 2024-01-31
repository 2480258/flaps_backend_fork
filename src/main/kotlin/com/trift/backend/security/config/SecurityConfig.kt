package com.trift.backend.security.config

import com.trift.backend.domain.Role
import com.trift.backend.security.CorsFilter
import com.trift.backend.security.jwt.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }
}

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    open fun filterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf {
                it.disable()
            }
            .cors {
                it.configurationSource {
                    CorsConfiguration().applyPermitDefaultValues()
                }
                it.disable()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/login/**", "/api/v1/refresh/**","/api/v1/city/**", "/api/v1/country/**", "/error", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                it.requestMatchers("/api/v1/project/**", "/api/v1/place/**", "/api/v1/bucket/**", "/api/v1/graph/**", "/api/v1/user/**", "/api/v1/route/**")
                    .hasAnyRole(Role.GUEST.name, Role.MEMBER.name, Role.ADMIN.name)
                it.requestMatchers("/api/v1/admin/**").hasAnyRole(Role.ADMIN.name)
                it.anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.FORBIDDEN))
            }
            .addFilterBefore(JwtAuthFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(CorsFilter(), LogoutFilter::class.java)
        return http.build()
    }
}