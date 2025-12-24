package ru.redeyed.cloudstorage.test.integration;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@IntegrationTest
public abstract class BaseIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES_TEST_CONTAINER = new PostgreSQLContainer<>("postgres:18.1");

    private static final RedisContainer REDIS_TEST_CONTAINER = new RedisContainer("redis:8.4.0");

    private static final MinIOContainer MINIO_TEST_CONTAINER = new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z-cpuv1");

    @BeforeAll
    static void startContainers() {
        POSTGRES_TEST_CONTAINER.start();
        REDIS_TEST_CONTAINER.start();
        MINIO_TEST_CONTAINER.start();
    }

    @DynamicPropertySource
    static void setContainersProperties(DynamicPropertyRegistry registry) {
        setPostgresProperties(registry);
        setRedisProperties(registry);
        setMinioProperties(registry);
    }

    private static void setPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_TEST_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_TEST_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_TEST_CONTAINER::getPassword);
    }

    private static void setRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_TEST_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_TEST_CONTAINER::getFirstMappedPort);
    }

    private static void setMinioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", MINIO_TEST_CONTAINER::getS3URL);
        registry.add("minio.accessKey", MINIO_TEST_CONTAINER::getUserName);
        registry.add("minio.secretKey", MINIO_TEST_CONTAINER::getPassword);
    }
}
