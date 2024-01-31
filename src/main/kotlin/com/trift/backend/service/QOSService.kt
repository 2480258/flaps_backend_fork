package com.trift.backend.service

import com.trift.backend.domain.Authority
import com.trift.backend.domain.Role
import com.trift.backend.repository.UserRepository
import com.trift.backend.security.TokenDTO
import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.distributed.proxy.ClientSideConfig
import io.github.bucket4j.distributed.versioning.Version
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Duration
import java.time.Duration.ofSeconds


interface QOSService {
    fun <T> consumesTokenWith(token: TokenDTO, func: () -> T): T

    fun getLeftToken(token: TokenDTO): Long
}

@Component
class QOSServiceImpl constructor(
    @Value("\${optimization.max_tokens}") private val maxTokens: Int,
    @Value("\${optimization.refill_tokens_per_day}") private val refillTokensPerDay: Int,
    @Value("\${bucket.redis.host}") private val host: String,
    @Value("\${bucket.redis.port}") private val port: Int
) : QOSService {

    private var statefulRedisConnection : StatefulRedisConnection<String, ByteArray>? = null


    @Autowired
    lateinit var userRepository: UserRepository

    private var bucketConfiguration : BucketConfiguration? = null

    private var proxyManager: LettuceBasedProxyManager<String>? = null

    private fun prepareIfNot() {
        if((statefulRedisConnection != null) or (bucketConfiguration != null) or (proxyManager != null)) return

        statefulRedisConnection =
            RedisClient.create("redis://${host}:${port}").connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE))

        bucketConfiguration = BucketConfiguration.builder().addLimit {
            it.capacity(maxTokens.toLong()).refillGreedy(refillTokensPerDay.toLong(), Duration.ofDays(1))
        }.build()

        proxyManager = LettuceBasedProxyManager.builderFor(statefulRedisConnection)
            .withExpirationStrategy(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(ofSeconds(10)))
            .build()
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    private fun createBucket(token: TokenDTO): Bucket {
        prepareIfNot()

        val user =
            userRepository.findByuserId(token.getUserIdOrThrow())
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "유저 토큰 오류. 다시 로그인해주세요.")

        if (user.role!! == Role.GUEST) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "게스트는 버킷이 없습니다.")
        }

        return proxyManager!!.builder().build("OPU${user.userId!!}") { bucketConfiguration }
    }

    override fun <T> consumesTokenWith(token: TokenDTO, func: () -> T): T {
        val bucket = createBucket(token)
        if (bucket.tryConsume(1)) {
            try {
                return func()
            } catch (e: Exception) {
                bucket.addTokens(1)
                throw e
            }
        } else {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "No tokens for bucket")
        }
    }

    override fun getLeftToken(token: TokenDTO): Long {
        val bucket = createBucket(token)

        return bucket.availableTokens
    }
}