package integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kuraterut.productservice.ProductServiceApplication;
import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.requests.CreateProductRequest;
import org.kuraterut.productservice.service.CategoryService;
import org.kuraterut.productservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

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
            .withPassword("test")
            .withStartupTimeout(Duration.ofSeconds(60));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)));

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

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
    private CategoryService categoryService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void printRedisCacheContents() {
        // Получаем все ключи по шаблону (для кэша 'products')
        Set<String> keys = redisTemplate.keys("products::*");

        if (keys != null) {
            keys.forEach(key -> {
                Object value = redisTemplate.opsForValue().get(key);
                System.out.println("Key: " + key + " | Value: " + value);
            });
        }
    }

    @Test
//    @Disabled("Temporarily disabled due to Redis issues in Jenkins environment")
    void cacheWorksForGetAllProducts() {
        CreateCategoryRequest createCategoryRequest = new CreateCategoryRequest(
                "testCategoryName",
                "testCategoryDescription"
        );
        categoryService.createCategory(createCategoryRequest);

        CreateProductRequest createProductRequest = new CreateProductRequest(
                "testProductName",
                "testProductDescription",
                BigDecimal.TEN,
                "testCategoryName",
                5L
        );
        productService.createProduct(createProductRequest, 1L);

        var pageable = PageRequest.of(0, 5);

        // First call - should populate cache
        var firstCall = productService.getAllProducts(pageable);


        // Verify cache entry exists
        await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    printRedisCacheContents();
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

