package com.trift.backend.config

import com.github.fppt.jedismock.RedisServer
import io.lettuce.core.RedisClient
import org.geojson.GeoJsonObject
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisCommands
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.function.Supplier




@Configuration
@EnableRedisRepositories
@Profile("!(dev | prod | test)")
class LocalRedisConfig {

    val server: RedisServer = RedisServer
        .newRedisServer()
        .start()

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(server.host, server.bindPort)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val redisTemplate = RedisTemplate<String, String>()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.connectionFactory = redisConnectionFactory()
        return redisTemplate
    }
}

@Configuration
@EnableRedisRepositories
@Profile("(dev | prod | test)")
class RemoteRedisConfig {
    @Value("\${spring.redis.host}")
    private val host: String? = null

    @Value("\${spring.redis.port}")
    private val port = 0

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(host!!, port)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val redisTemplate = RedisTemplate<String, String>()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.connectionFactory = redisConnectionFactory()
        return redisTemplate
    }
}


@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate(restTemplateBuilder: RestTemplateBuilder): RestTemplate {
        return restTemplateBuilder
            .requestFactory(
                Supplier<ClientHttpRequestFactory> {
                    BufferingClientHttpRequestFactory(
                        SimpleClientHttpRequestFactory()
                    )
                }
            ).additionalMessageConverters(
                StringHttpMessageConverter(
                    StandardCharsets.UTF_8
                )
            ).build()
    }
}


@Configuration
@EnableCaching
class RedisCacheConfig {

    @Bean
    @Primary
    fun contentCacheManager(cf: RedisConnectionFactory?): CacheManager {
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer<String>(StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer<Any>(
                    GenericJackson2JsonRedisSerializer()
                )
            ) // Value Serializer 변경
            .entryTtl(Duration.ofMinutes(3000000L)) // 캐시 수명 3000000분
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(cf!!)
            .cacheDefaults(redisCacheConfiguration).build()
    }

    @Bean
    fun geoJsonCacheManager(cf: RedisConnectionFactory?): CacheManager {
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer<String>(StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    Jackson2JsonRedisSerializer(GeoJsonObject::class.java)
                )
            )
            .entryTtl(Duration.ofMinutes(3000000L)) // 캐시 수명 3000000분
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(cf!!)
            .cacheDefaults(redisCacheConfiguration).build()
    }
}

