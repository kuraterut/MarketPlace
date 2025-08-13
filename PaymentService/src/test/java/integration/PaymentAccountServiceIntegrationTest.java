package integration;

import org.junit.jupiter.api.*;
import org.kuraterut.paymentservice.PaymentServiceApplication;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.exception.model.PaymentAccountAlreadyExistsException;
import org.kuraterut.paymentservice.exception.model.PaymentAccountIsNotEmptyException;
import org.kuraterut.paymentservice.exception.model.PaymentAccountNotFoundException;
import org.kuraterut.paymentservice.model.entity.PaymentAccount;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.service.PaymentAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = PaymentServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = PaymentAccountServiceIntegrationTest.Initializer.class)
@Transactional
@Testcontainers
@EnableCaching
@TestPropertySource(locations = "classpath:application-test.yaml")
public class PaymentAccountServiceIntegrationTest {

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

    public static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            TestPropertyValues.of(
                    "spring.jpa.hibernate.ddl-auto=none",
                    "spring.liquibase.enabled=true"
            ).applyTo(context.getEnvironment());
        }
    }

    @Autowired
    private PaymentAccountService service;

    @Autowired
    private PaymentAccountRepository repository;

    @Test
    void createPaymentAccount_success() {
        PaymentAccountResponse response = service.createPaymentAccount(100L);
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(100L);
    }

    @Test
    void createPaymentAccount_duplicate_throwsException() {
        service.createPaymentAccount(200L);
        assertThatThrownBy(() -> service.createPaymentAccount(200L))
                .isInstanceOf(PaymentAccountAlreadyExistsException.class);
    }

    @Test
    void getPaymentAccountById_success() {
        PaymentAccountResponse created = service.createPaymentAccount(300L);
        PaymentAccountResponse found = service.getPaymentAccountById(created.getId());
        assertThat(found.getUserId()).isEqualTo(300L);
    }

    @Test
    void deletePaymentAccount_success() {
        PaymentAccountResponse created = service.createPaymentAccount(400L);
        service.deletePaymentAccountById(created.getId());
        assertThat(repository.findById(created.getId())).isEmpty();
    }

    @Test
    void deletePaymentAccount_withBalance_throwsException() {
        PaymentAccountResponse created = service.createPaymentAccount(500L);
        // вручную увеличиваем баланс
        PaymentAccount acc = repository.findById(created.getId()).get();
        acc.setBalance(BigDecimal.TEN);
        repository.save(acc);

        assertThatThrownBy(() -> service.deletePaymentAccountById(created.getId()))
                .isInstanceOf(PaymentAccountIsNotEmptyException.class);
    }

    @Test
    void depositPaymentAccount_success() {
        PaymentAccountResponse created = service.createPaymentAccount(600L);
        PaymentAccountResponse updated = service.depositPaymentAccountByUserId(600L, BigDecimal.valueOf(100));
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void withdrawPaymentAccount_success() {
        PaymentAccountResponse created = service.createPaymentAccount(700L);
        service.depositPaymentAccountByUserId(700L, BigDecimal.valueOf(200));
        PaymentAccountResponse updated = service.withdrawPaymentAccountByUserId(700L, BigDecimal.valueOf(50));
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void getPaymentAccountByUserId_notFound_throwsException() {
        assertThatThrownBy(() -> service.getPaymentAccountByUserId(9999L))
                .isInstanceOf(PaymentAccountNotFoundException.class);
    }
}
