package com.trift.backend

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@OpenAPIDefinition(servers = [Server(url = "/", description = "Default Server URL")])
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
class BackendApplication

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}
