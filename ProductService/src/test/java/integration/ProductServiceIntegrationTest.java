package integration;

import org.junit.jupiter.api.Test;
import org.kuraterut.productservice.ProductServiceApplication;
import org.kuraterut.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = ProductServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableCaching
@TestPropertySource(locations = "classpath:application-test.yaml")
public class ProductServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> keydb = new GenericContainer<>("eqalpha/keydb:latest")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort()
                    .withStartupTimeout(Duration.ofSeconds(30)));

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", keydb::getHost);
        registry.add("spring.data.redis.port", keydb::getFirstMappedPort);

        registry.add("spring.cache.type", () -> "redis");
        registry.add("spring.cache.redis.time-to-live", () -> "30000"); // 30 seconds


        registry.add("jwt.secret", () -> "test-secret-key-1234567890hasfhasfhasfhashhshasfhha");
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.kafka.listener.auto-startup", () -> false);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.properties.enable.auto.commit", () -> false);
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void cacheWorksForGetAllProducts() {
        var pageable = PageRequest.of(0, 5);

        // First call - should populate cache
        var firstCall = productService.getAllProducts(pageable);

        // Verify cache entry exists
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var cache = cacheManager.getCache("products");
                    assertThat(cache).isNotNull();
                    var cachedValue = cache.get("all_products_0_5");
                    assertThat(cachedValue).isNotNull();
                    assertThat(cachedValue.get()).isEqualTo(firstCall);
                });

        // Second call - should come from cache
        var secondCall = productService.getAllProducts(pageable);
        assertThat(secondCall).isEqualTo(firstCall);
    }
}

