package integration;

import org.junit.jupiter.api.Test;
import org.kuraterut.authservice.AuthServiceApplication;
import org.kuraterut.authservice.dto.requests.LoginRequest;
import org.kuraterut.authservice.dto.requests.RegisterRequest;
import org.kuraterut.authservice.dto.responses.LoginResponse;
import org.kuraterut.authservice.dto.responses.RegisterResponse;
import org.kuraterut.authservice.exception.model.UserNotFoundException;
import org.kuraterut.authservice.model.event.UserRegistrationEvent;
import org.kuraterut.authservice.model.utils.Role;
import org.kuraterut.authservice.repository.UserRepository;
import org.kuraterut.authservice.service.LoginService;
import org.kuraterut.authservice.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest(classes = AuthServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableCaching
@TestPropertySource(locations = "classpath:application-test.yaml")
@Import(TestConfig.class)
public class LoginRegistrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    @Container
    static GenericContainer<?> keydb = new GenericContainer<>("eqalpha/keydb:latest").withExposedPorts(6379);

    @Autowired
    private LoginService loginService;
    @Autowired
    private RegisterService registerService;
    @Autowired
    private UserRepository userRepository;


    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        postgres.start();
        keydb.start();
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

    @Test
    void registerAndLogin_success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("test@example.com", "pass", Role.CUSTOMER);
        RegisterResponse regResponse = registerService.register(registerRequest);

        assertThat(regResponse.getToken()).isNotEmpty();

        LoginRequest loginRequest = new LoginRequest("test@example.com", "pass");
        LoginResponse loginResponse = loginService.login(loginRequest);

        assertThat(loginResponse.getToken()).isNotEmpty();
    }

    @Test
    void login_nonExistingUser_throws() {
        LoginRequest loginRequest = new LoginRequest("nope@example.com", "pass");
        assertThatThrownBy(() -> loginService.login(loginRequest))
                .isInstanceOf(UserNotFoundException.class);
    }
}