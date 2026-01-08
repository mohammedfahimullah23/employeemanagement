package com.example.learningjava.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

@EnableCaching
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.username}")
    private String redisUsername;

    @Value("${spring.redis.port}")
    private String redisPassword;

    // 1️⃣ Lettuce TLS config (DEV ONLY)
//    @Bean
//    public LettuceClientConfiguration lettuceClientConfiguration() {
//        return LettuceClientConfiguration.builder()
//                .useSsl()
//                .disablePeerVerification() // DEV ONLY
//                .build();
//
//    }

    // PROD
    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration() {
        return LettuceClientConfiguration.builder()
                .useSsl()
                .build();
    }

    // 2️⃣ Redis connection details (Azure Redis Enterprise)
    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            LettuceClientConfiguration lettuceClientConfiguration) {

        RedisStandaloneConfiguration redisConfig =
                new RedisStandaloneConfiguration(
                        redisHost,
                        redisPort
                );

        redisConfig.setUsername(redisUsername);
        redisConfig.setPassword(redisPassword);

        return new LettuceConnectionFactory(redisConfig, lettuceClientConfiguration);
    }

    // 3️⃣ RedisTemplate
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    // 4️⃣ Cache manager
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer())
                        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

}
