package integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuraterut.productservice.ProductServiceApplication;
import org.kuraterut.productservice.dto.requests.CreateCategoryRequest;
import org.kuraterut.productservice.dto.responses.CategoryResponse;
import org.kuraterut.productservice.model.entity.Category;
import org.kuraterut.productservice.repository.CategoryRepository;
import org.kuraterut.productservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = ProductServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableCaching
@TestPropertySource(locations = "classpath:application-test.yaml")
public class CategoryServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> keydb = new GenericContainer<>("eqalpha/keydb:latest")
            .withExposedPorts(6379);


    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", keydb::getHost);
        registry.add("spring.data.redis.port", () -> keydb.getMappedPort(6379));
        registry.add("jwt.secret", () -> "test-secret-key-1234567890hasfhasfhasfhashhshasfhha");
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.kafka.listener.auto-startup", () -> false);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.properties.enable.auto.commit", () -> false);
    }

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryService categoryService;

    private final OffsetDateTime testCreatedAtDateTime = OffsetDateTime.of(2025, 8, 12, 0, 0, 0, 0, ZoneOffset.ofHours(1));
    private final OffsetDateTime testUpdatedAtDateTime = OffsetDateTime.of(2025, 8, 12, 0, 0, 0, 0, ZoneOffset.ofHours(1));

    Category getTestCategory() {
        return new Category(
                1L,
                "categoryTestName",
                "categoryTestDescription",
                testCreatedAtDateTime,
                testUpdatedAtDateTime,
                new HashSet<>()
        );
    }


    @BeforeEach
    void setup() {
        categoryRepository.deleteAll();
    }

    @Test
    void createAndGetCategory_success() {
        CreateCategoryRequest request = new CreateCategoryRequest("testCategoryName", "testCategoryDescription");

        CategoryResponse created = categoryService.createCategory(request);
        assertThat(created.getName()).isEqualTo("testCategoryName");

        CategoryResponse fetched = categoryService.getCategory(created.getId());
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo(created.getName());
        assertThat(fetched.getDescription()).isEqualTo(created.getDescription());
        assertThat(OffsetDateTime.parse(fetched.getCreatedAt()))
                .isEqualTo(OffsetDateTime.parse(created.getCreatedAt()));
        assertThat(OffsetDateTime.parse(fetched.getUpdatedAt()))
                .isEqualTo(OffsetDateTime.parse(created.getUpdatedAt()));
    }

    @Test
    void deleteCategoryById_removesCategoryAndClearsProducts() {
        Category toSaveCategory = getTestCategory();
        toSaveCategory.setId(null);
        Category category = categoryRepository.saveAndFlush(toSaveCategory);

        categoryService.deleteCategory(category.getId());

        assertThat(categoryRepository.findById(category.getId())).isEmpty();
    }

    @Test
    void cacheIsUsedForGetCategory() {
        Category toSaveCategory = getTestCategory();
        toSaveCategory.setId(null);
        Category category = categoryRepository.saveAndFlush(toSaveCategory);

        // Первый вызов — из БД
        CategoryResponse first = categoryService.getCategory(category.getId());
        // Второй вызов — должен идти из кэша
        CategoryResponse second = categoryService.getCategory(category.getId());

        assertThat(first).isEqualTo(second);
    }
}
