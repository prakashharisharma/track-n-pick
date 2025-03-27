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
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Autowired
    private RedisConfigurationProperties redisProperties;

    @Value("${spring.redis.key.prefix:}")
    private String redisPrefix;

    @Value("${spring.redis.connect-timeout:10000}")
    private long timeout;

    /**
     * Creates connection factory for stand alone redis
     *
     * <p>useful for local or test environment
     *
     * @param redisConfiguration
     * @return
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.cluster",
            name = "enabled",
            havingValue = "false",
            matchIfMissing = true)
    LettuceConnectionFactory redisConnectionFactory(RedisStandaloneConfiguration redisConfiguration) {

        LettuceClientConfiguration clientConfig =
                LettuceClientConfiguration.builder().commandTimeout(Duration.ofMillis(timeout)).build();

        return new LettuceConnectionFactory(redisConfiguration, clientConfig);
    }

    /**
     * Creates connection factory with cluster nodes
     *
     * <p>To be used in AWS
     *
     * @param redisConfiguration
     * @return
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.cluster",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = false)
    LettuceConnectionFactory redisClusterConnectionFactory(
            RedisClusterConfiguration redisConfiguration) {

        LettuceClientConfiguration clientConfig =
                LettuceClientConfiguration.builder().readFrom(ReadFrom.REPLICA_PREFERRED).build();

        return new LettuceConnectionFactory(redisConfiguration, clientConfig);
    }

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

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.redis.cluster",
            name = "enabled",
            havingValue = "false",
            matchIfMissing = false)
    RedisStandaloneConfiguration redisstandaloneConfiguration() {

        String node = redisProperties.getNodes().get(0);

        String[] hostPort = node.split(":");

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(hostPort[0]);
        redisStandaloneConfiguration.setPort(Integer.parseInt(hostPort[1]));

        return redisStandaloneConfiguration;
    }

    @Bean
    @Primary
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

    // Default cache manager is infinite
    @Primary
    @Bean(name = CacheManagerName.DEFAULT)
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig().prefixCacheNameWith(redisPrefix))
                .build();
    }

    /**
     * Expires in 24 hours
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean(name = CacheManagerName.CACHE_24_HOUR)
    public CacheManager cacheManager24Hour(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofHours(24);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig()
                                .prefixCacheNameWith(redisPrefix)
                                .entryTtl(expiration))
                .build();
    }

    /**
     * Expires in 48 hours
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean(name = CacheManagerName.CACHE_48_HOUR)
    public CacheManager cacheManager48Hour(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofHours(48);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig()
                                .prefixCacheNameWith(redisPrefix)
                                .entryTtl(expiration))
                .build();
    }

    /**
     * Expires in 72 hours
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean(name = CacheManagerName.CACHE_72_HOUR)
    public CacheManager cacheManager72Hour(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofHours(72);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig()
                                .prefixCacheNameWith(redisPrefix)
                                .entryTtl(expiration))
                .build();
    }

    @Bean(name = CacheManagerName.CACHE_5_MINUTE)
    public CacheManager cacheManager5Minute(RedisConnectionFactory redisConnectionFactory) {
        Duration expiration = Duration.ofMinutes(5);
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(
                        RedisCacheConfiguration.defaultCacheConfig()
                                .prefixCacheNameWith(redisPrefix)
                                .entryTtl(expiration))
                .build();
    }
}

