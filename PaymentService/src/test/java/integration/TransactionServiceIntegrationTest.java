package integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuraterut.paymentservice.PaymentServiceApplication;
import org.kuraterut.paymentservice.dto.request.CreateTransactionRequest;
import org.kuraterut.paymentservice.dto.response.TransactionResponse;
import org.kuraterut.paymentservice.exception.model.TransactionNotFoundException;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.repository.TransactionRepository;
import org.kuraterut.paymentservice.service.PaymentAccountService;
import org.kuraterut.paymentservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = PaymentServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableCaching
@TestPropertySource(locations = "classpath:application-test.yaml")
public class TransactionServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> keydb = new GenericContainer<>("eqalpha/keydb:latest")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        postgres.start();
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
    //TODO Убрать все var
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PaymentAccountService paymentAccountService;

    @Autowired
    private CacheManager cacheManager;

    private Long userId;

    @BeforeEach
    @Transactional
    void setUp() {
        userId = Math.abs(UUID.randomUUID().getMostSignificantBits());
        paymentAccountService.createPaymentAccount(userId);
    }

    @Test
    void createTransaction_success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setTransactionType(TransactionType.DEPOSIT);

        TransactionResponse response = transactionService.createTransaction(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo("100");
        assertThat(response.getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void getTransactionById_success() {
        var request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(50));
        request.setTransactionType(TransactionType.WITHDRAW);

        var created = transactionService.createTransaction(request, userId);

        var found = transactionService.getTransactionById(created.getId(), userId);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
    }

    @Test
    void getTransactionById_notFound() {
        assertThatThrownBy(() -> transactionService.getTransactionById(9999L, userId))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void getAllTransactions_success_and_cache() {
        var request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.TEN);
        request.setTransactionType(TransactionType.DEPOSIT);
        transactionService.createTransaction(request, userId);

        var pageable = PageRequest.of(0, 10);

        transactionService.getAllTransactions(userId, pageable);
        transactionService.getAllTransactions(userId, pageable); // второй вызов для кэша

        var cacheKey = "all_transactions_user_" + userId + "_page_0_size_10";
        assertThat(cacheManager.getCache("transactions").get(cacheKey)).isNotNull();
    }

    @Test
    void getTransactionsByAmountBetween_success() {
        var request = new CreateTransactionRequest();
        request.setAmount(BigDecimal.valueOf(200));
        request.setTransactionType(TransactionType.DEPOSIT);
        transactionService.createTransaction(request, userId);

        var pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        var result = transactionService.getTransactionsByAmountBetween(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(300),
                userId,
                pageable
        );

        assertThat(result.getTransactions()).isNotEmpty();
    }
}
