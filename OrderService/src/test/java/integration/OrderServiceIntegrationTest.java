package integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuraterut.orderservice.OrderServiceApplication;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderListResponse;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.event.dto.OrderItemDto;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.kuraterut.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OrderServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableCaching
@ImportAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class,
        org.springframework.kafka.annotation.KafkaBootstrapConfiguration.class
})
@TestPropertySource(locations = "classpath:application-test.yaml")
public class OrderServiceIntegrationTest {

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
        // Kafka отключаем
        registry.add("spring.kafka.bootstrap-servers", () -> "dummy:1234");
        registry.add("spring.kafka.listener.auto-startup", () -> false);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    }


    @Autowired
    private OrderService orderService;

    @Autowired
    private CacheManager cacheManager;

    private Long userId;

    @BeforeEach
    @Transactional
    void setUp() {
        userId = Math.abs(System.nanoTime()); // уникальный ID для каждого теста
        CreateOrderRequest request = new CreateOrderRequest();
        List<OrderItemDto> items = new ArrayList<>();
        OrderItemDto item1 = new OrderItemDto(1L, 5L);
        OrderItemDto item2 = new OrderItemDto(2L, 5L);
        items.add(item1);
        items.add(item2);
        request.setItems(items);
        orderService.createOrder(request, userId);
    }

    @Test
    void createOrder_success() {
        CreateOrderRequest request = new CreateOrderRequest();
        List<OrderItemDto> items = new ArrayList<>();
        OrderItemDto item1 = new OrderItemDto(1L, 5L);
        OrderItemDto item2 = new OrderItemDto(2L, 5L);
        items.add(item1);
        items.add(item2);
        request.setItems(items);

        OrderResponse response = orderService.createOrder(request, userId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void getOrderById_success_and_cache() {
        Pageable pageable = PageRequest.of(0, 5);
        OrderListResponse listResponse = orderService.getAllOrders(pageable);
        Long orderId = listResponse.getOrders().get(0).getId();

        orderService.getOrderById(orderId);
        orderService.getOrderById(orderId); // для кэша

        String cacheKey = "order_by_id_" + orderId;
        assertThat(cacheManager.getCache("orders").get(cacheKey)).isNotNull();
    }

    @Test
    void getAllOrders_success_and_cache() {
        Pageable pageable = PageRequest.of(0, 5);

        orderService.getAllOrders(pageable);
        orderService.getAllOrders(pageable);

        String cacheKey = "all_orders_page_0_size_5";
        assertThat(cacheManager.getCache("orders").get(cacheKey)).isNotNull();
    }

    @Test
    void getAllOrdersByUserId_success_and_cache() {
        Pageable pageable = PageRequest.of(0, 5);

        orderService.getAllOrdersByUserId(userId, pageable);
        orderService.getAllOrdersByUserId(userId, pageable);

        String cacheKey = "orders_user_" + userId + "_page_0";
        assertThat(cacheManager.getCache("orders").get(cacheKey)).isNotNull();
    }

    @Test
    void getAllOrdersByOrderStatus_success_and_cache() {
        Pageable pageable = PageRequest.of(0, 5);

        orderService.getAllOrdersByOrderStatus(OrderStatus.CREATED, pageable);
        orderService.getAllOrdersByOrderStatus(OrderStatus.CREATED, pageable);

        String cacheKey = "orders_status_CREATED_page_0";
        assertThat(cacheManager.getCache("orders").get(cacheKey)).isNotNull();
    }

    @Test
    void getAllOrdersByCreatedAtAfter_success_and_cache() {
        Pageable pageable = PageRequest.of(0, 5);
        OffsetDateTime after = OffsetDateTime.now().minusDays(1);

        orderService.getAllOrdersByCreatedAtAfter(after, pageable);
        orderService.getAllOrdersByCreatedAtAfter(after, pageable);

        String cacheKey = "orders_after_" + after.toEpochSecond() + "_page_0";
        assertThat(cacheManager.getCache("orders").get(cacheKey)).isNotNull();
    }
}
