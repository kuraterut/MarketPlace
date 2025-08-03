package org.kuraterut.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderListResponse;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.exception.model.OrderNotFoundException;
import org.kuraterut.orderservice.mapper.OrderMapper;
import org.kuraterut.orderservice.model.entity.Order;
import org.kuraterut.orderservice.model.event.outbox.CreateOrderEventOutbox;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.kuraterut.orderservice.model.event.OrderCreatedEvent;
import org.kuraterut.orderservice.repository.OrderOutboxRepository;
import org.kuraterut.orderservice.repository.OrderRepository;
import org.kuraterut.orderservice.usecases.CreateOrderUseCase;
import org.kuraterut.orderservice.usecases.GetOrderUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "orders")
public class OrderService implements CreateOrderUseCase, GetOrderUseCase{
    private final OrderRepository orderRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderMapper orderMapper;

    private final KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate;

    @Value("${kafka-topics.order-created}")
    private String orderCreatedTopic;

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public OrderResponse createOrder(CreateOrderRequest request, Long userId)  {
        Order order = orderMapper.toEntity(request, userId);

        order.setStatus(OrderStatus.CREATED);
        order = orderRepository.saveAndFlush(order);

        CreateOrderEventOutbox createOrderEventOutbox = orderMapper.toOutbox(order);
        orderOutboxRepository.save(createOrderEventOutbox);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_orders_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public OrderListResponse getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'order_by_id_' + #orderId")
    public OrderResponse getOrderById(Long orderId)  {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found by id: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_user_' + #userId + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByUserId(Long userId, Pageable pageable)  {
        Page<Order> orders = orderRepository.findAllByUserId(userId, pageable);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_status_' + #orderStatus.name() + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByOrderStatus(OrderStatus orderStatus, Pageable pageable)  {
        Page<Order> orders = orderRepository.findAllByStatus(orderStatus, pageable);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_status_' + #orderStatus.name() + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByOrderStatus(OrderStatus orderStatus, Long userId, Pageable pageable)  {
        Page<Order> orders = orderRepository.findAllByStatusAndUserId(orderStatus, userId, pageable);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_after_' + #afterCreatedAt.toEpochSecond() + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Pageable pageable)  {
        Page<Order> orders = orderRepository.findAllByCreatedAtAfter(afterCreatedAt, pageable);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_after_' + #afterCreatedAt.toEpochSecond() + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Long userId, Pageable pageable)  {
        Page<Order> orders = orderRepository.findAllByCreatedAtAfterAndUserId(afterCreatedAt, userId, pageable);
        return orderMapper.toResponses(orders);
    }

    @Transactional
    @Scheduled(fixedRateString = "${scheduling.process-create-order-rate}")
    public void processCreateOrderEvent() {
        //TODO N+1
        List<CreateOrderEventOutbox> createOrderEventOutboxList = orderOutboxRepository.findTop100ByProcessedIsFalse();
        for (CreateOrderEventOutbox createOrderEventOutbox : createOrderEventOutboxList){
            try{
                OrderCreatedEvent event = new OrderCreatedEvent();
                event.setUserId(createOrderEventOutbox.getOrder().getUserId());
                event.setOrderId(createOrderEventOutbox.getOrder().getId());
                event.setItems(orderMapper.toResponses(createOrderEventOutbox.getOrder().getItems()));

                orderCreatedEventKafkaTemplate.send(orderCreatedTopic, event).get();

                orderOutboxRepository.markAsProcessed(createOrderEventOutbox.getId());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to process outbox with id: {}", createOrderEventOutbox.getId(), e);
            }
        }
    }

}
