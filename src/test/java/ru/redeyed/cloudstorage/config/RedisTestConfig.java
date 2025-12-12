package ru.redeyed.cloudstorage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.test.context.ActiveProfiles;

@Configuration
@ActiveProfiles("test")
public class RedisTestConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        var redisTemplate = new RedisTemplate<String, Object>();

        var stringRedisSerializer = new StringRedisSerializer();
        var securityContextSerializer = new JacksonJsonRedisSerializer<>(SecurityContext.class);

        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(securityContextSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public RedisIndexedSessionRepository redisSessionRepository(RedisTemplate<String, Object> redisTemplate) {
        return new RedisIndexedSessionRepository(redisTemplate);
    }
}
