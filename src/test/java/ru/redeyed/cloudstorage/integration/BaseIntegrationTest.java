package ru.redeyed.cloudstorage.integration;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@IntegrationTest
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES_TEST_CONTAINER = new PostgreSQLContainer<>("postgres:18.1");

    @Container
    @ServiceConnection
    private static final RedisContainer REDIS_TEST_CONTAINER = new RedisContainer("redis:8.4.0");
}
