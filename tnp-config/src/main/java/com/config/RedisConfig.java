package com.example.config;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class RedisConfig {

    @Autowired
    private RedisConfigurationProperties redisProperties;

    @Value("${spring.redis.key.prefix:cache:}")
    private String redisPrefix;

    @Value("${spring.redis.connect-timeout:10000}")
    private long timeout;

    /**
     * Creates connection factory for standalone Redis.
     * Useful for local or test environment.
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.cluster",
            name = "enabled",
            havingValue = "false",
            matchIfMissing = true)
    LettuceConnectionFactory redisConnectionFactory(RedisStandaloneConfiguration redisConfiguration) {

        LettuceClientConfiguration clientConfig =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofMillis(timeout))
                        .build();

        return new LettuceConnectionFactory(redisConfiguration, clientConfig);
    }

    /**
     * Creates connection factory for Redis Cluster.
     * Used in AWS or production environments.
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.cluster",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = false)
    LettuceConnectionFactory redisClusterConnectionFactory(RedisClusterConfiguration redisConfiguration) {

        LettuceClientConfiguration clientConfig =
                LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofMillis(timeout))
                        .readFrom(ReadFrom.REPLICA_PREFERRED)
                        .build();

        return new LettuceConnectionFactory(redisConfiguration, clientConfig);
    }

    /**
     * Configures Redis Cluster with nodes.
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.cluster",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = false)
    RedisClusterConfiguration redisConfiguration() {

        RedisClusterConfiguration redisClusterConfiguration =
                new RedisClusterConfiguration(redisProperties.getNodes());
        redisClusterConfiguration.setMaxRedirects(redisProperties.getMaxRedirects());

        return redisClusterConfiguration;
    }

    /**
     * Configures Redis Standalone mode.
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.cluster",
            name = "enabled",
            havingValue = "false",
            matchIfMissing = true)
    RedisStandaloneConfiguration redisStandaloneConfiguration() {

        List<String> nodes = redisProperties.getNodes();
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Redis standalone node is not configured properly.");
        }

        String node = nodes.get(0);
        String[] hostPort = node.split(":");

        if (hostPort.length != 2) {
            throw new IllegalArgumentException("Invalid Redis standalone node format: " + node);
        }

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(hostPort[0]);
        redisStandaloneConfiguration.setPort(Integer.parseInt(hostPort[1]));

        return redisStandaloneConfiguration;
    }

    /**
     * Configures the RedisTemplate for key-value operations.
     */
    @Bean
    @Primary
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    /**
     * Default cache manager with no expiration.
     */
    @Primary
    @Bean(name = CacheManagerNameConstants.DEFAULT)
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .prefixCacheNameWith(redisPrefix))
                .build();
    }

    /**
     * Cache manager with 24-hour expiration.
     */
    @Bean(name = CacheManagerNameConstants.CACHE_24_HOUR)
    public CacheManager cacheManager24Hour(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofHours(24);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .prefixCacheNameWith(redisPrefix)
                        .entryTtl(expiration))
                .build();
    }

    /**
     * Cache manager with 48-hour expiration.
     */
    @Bean(name = CacheManagerNameConstants.CACHE_48_HOUR)
    public CacheManager cacheManager48Hour(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofHours(48);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .prefixCacheNameWith(redisPrefix)
                        .entryTtl(expiration))
                .build();
    }

    /**
     * Cache manager with 72-hour expiration.
     */
    @Bean(name = CacheManagerNameConstants.CACHE_72_HOUR)
    public CacheManager cacheManager72Hour(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofHours(72);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .prefixCacheNameWith(redisPrefix)
                        .entryTtl(expiration))
                .build();
    }

    /**
     * Cache manager with 5-minute expiration.
     */
    @Bean(name = CacheManagerNameConstants.CACHE_5_MINUTE)
    public CacheManager cacheManager5Minute(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofMinutes(5);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .prefixCacheNameWith(redisPrefix)
                        .entryTtl(expiration))
                .build();
    }
}
