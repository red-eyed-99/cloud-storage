package ru.redeyed.cloudstorage.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import java.time.Duration;

@Configuration
@EnableRedisHttpSession
public class RedisConfig {

    @Value("${spring.data.redis.session-max-inactive-interval}")
    private Duration sessionMaxInactiveInterval;

    @Autowired
    public void setSessionMaxInactiveInterval(RedisSessionRepository redisSessionRepository) {
        redisSessionRepository.setDefaultMaxInactiveInterval(sessionMaxInactiveInterval);
    }
}
