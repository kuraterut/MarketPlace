package unit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderListResponse;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.exception.model.OrderNotFoundException;
import org.kuraterut.orderservice.mapper.OrderMapper;
import org.kuraterut.orderservice.model.entity.Order;
import org.kuraterut.orderservice.model.event.outbox.CreateOrderEventOutbox;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.kuraterut.orderservice.repository.OrderOutboxRepository;
import org.kuraterut.orderservice.repository.OrderRepository;
import org.kuraterut.orderservice.service.OrderService;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderOutboxRepository orderOutboxRepository;
    @Mock
    private OrderMapper orderMapper;

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, ?> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest request;
    private Order order;
    private OrderResponse orderResponse;
    private CreateOrderEventOutbox outbox;

    @BeforeEach
    void setUp() {
        request = new CreateOrderRequest();
        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        orderResponse = new OrderResponse();
        orderResponse.setId(1L);

        outbox = new CreateOrderEventOutbox();
    }

    @Test
    void createOrder_success() {
        when(orderMapper.toEntity(request, 100L)).thenReturn(order);
        when(orderRepository.saveAndFlush(order)).thenReturn(order);
        when(orderMapper.toOutbox(order)).thenReturn(outbox);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(request, 100L);

        assertThat(result).isEqualTo(orderResponse);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);

        verify(orderRepository).saveAndFlush(order);
        verify(orderOutboxRepository).save(outbox);
    }

    @Test
    void getAllOrders_success() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> orders = new PageImpl<>(List.of(order));
        OrderListResponse expectedResponse = new OrderListResponse(List.of(orderResponse));

        when(orderRepository.findAll(pageable)).thenReturn(orders);
        when(orderMapper.toResponses(orders)).thenReturn(expectedResponse);

        OrderListResponse result = orderService.getAllOrders(pageable);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getOrderById_found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(1L);

        assertThat(result).isEqualTo(orderResponse);
    }

    @Test
    void getOrderById_notFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getAllOrdersByUserId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = new PageImpl<>(List.of(order));
        OrderListResponse expectedResponse = new OrderListResponse(List.of(orderResponse));

        when(orderRepository.findAllByUserId(100L, pageable)).thenReturn(orders);
        when(orderMapper.toResponses(orders)).thenReturn(expectedResponse);

        OrderListResponse result = orderService.getAllOrdersByUserId(100L, pageable);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getAllOrdersByOrderStatus_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = new PageImpl<>(List.of(order));
        OrderListResponse expectedResponse = new OrderListResponse(List.of(orderResponse));

        when(orderRepository.findAllByStatus(OrderStatus.CREATED, pageable)).thenReturn(orders);
        when(orderMapper.toResponses(orders)).thenReturn(expectedResponse);

        OrderListResponse result = orderService.getAllOrdersByOrderStatus(OrderStatus.CREATED, pageable);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getAllOrdersByOrderStatusAndUserId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orders = new PageImpl<>(List.of(order));
        OrderListResponse expectedResponse = new OrderListResponse(List.of(orderResponse));

        when(orderRepository.findAllByStatusAndUserId(OrderStatus.CREATED, 100L, pageable)).thenReturn(orders);
        when(orderMapper.toResponses(orders)).thenReturn(expectedResponse);

        OrderListResponse result = orderService.getAllOrdersByOrderStatus(OrderStatus.CREATED, 100L, pageable);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getAllOrdersByCreatedAtAfter_success() {
        Pageable pageable = PageRequest.of(0, 10);
        OffsetDateTime after = OffsetDateTime.now().minusDays(1);
        Page<Order> orders = new PageImpl<>(List.of(order));
        OrderListResponse expectedResponse = new OrderListResponse(List.of(orderResponse));

        when(orderRepository.findAllByCreatedAtAfter(after, pageable)).thenReturn(orders);
        when(orderMapper.toResponses(orders)).thenReturn(expectedResponse);

        OrderListResponse result = orderService.getAllOrdersByCreatedAtAfter(after, pageable);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getAllOrdersByCreatedAtAfterAndUserId_success() {
        Pageable pageable = PageRequest.of(0, 10);
        OffsetDateTime after = OffsetDateTime.now().minusDays(1);
        Page<Order> orders = new PageImpl<>(List.of(order));
        OrderListResponse expectedResponse = new OrderListResponse(List.of(orderResponse));

        when(orderRepository.findAllByCreatedAtAfterAndUserId(after, 100L, pageable)).thenReturn(orders);
        when(orderMapper.toResponses(orders)).thenReturn(expectedResponse);

        OrderListResponse result = orderService.getAllOrdersByCreatedAtAfter(after, 100L, pageable);

        assertThat(result).isEqualTo(expectedResponse);
    }
}