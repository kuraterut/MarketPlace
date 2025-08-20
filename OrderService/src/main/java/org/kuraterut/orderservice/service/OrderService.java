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
        log.info("[OrderService:createOrder] Start createOrder");
        Order order = orderMapper.toEntity(request, userId);

        order.setStatus(OrderStatus.CREATED);
        order = orderRepository.saveAndFlush(order);
        log.info("[OrderService:createOrder] order created and saved successfully: {}", order);

        CreateOrderEventOutbox createOrderEventOutbox = orderMapper.toOutbox(order);
        orderOutboxRepository.save(createOrderEventOutbox);
        log.info("[OrderService:createOrder] Create Order Event Outbox created and saved successfully: {}",
                createOrderEventOutbox);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'all_orders_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public OrderListResponse getAllOrders(Pageable pageable) {
        log.info("[OrderService:getAllOrders] Start getAllOrders");
        Page<Order> orders = orderRepository.findAll(pageable);
        log.info("[OrderService:getAllOrders] Orders found");
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'order_by_id_' + #orderId")
    public OrderResponse getOrderById(Long orderId)  {
        log.info("[OrderService:getOrderById] Start getOrderById");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("[OrderService:getOrderById] Order not found with id: {}", orderId);
                    return new OrderNotFoundException("Order not found by id: " + orderId);
                });
        log.info("[OrderService:getOrderById] Order found: {}", order);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_user_' + #userId + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByUserId(Long userId, Pageable pageable)  {
        log.info("[OrderService:getAllOrdersByUserId] Start getAllOrdersByUserId");
        Page<Order> orders = orderRepository.findAllByUserId(userId, pageable);
        log.info("[OrderService:getAllOrdersByUserId] Orders found: {}", orders);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_status_' + #orderStatus.name() + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByOrderStatus(OrderStatus orderStatus, Pageable pageable)  {
        log.info("[OrderService:getAllOrdersByOrderStatus] Start getOrdersByOrderStatus");
        Page<Order> orders = orderRepository.findAllByStatus(orderStatus, pageable);
        log.info("[OrderService:getAllOrdersByOrderStatus] Orders found: {}", orders);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_status_' + #orderStatus.name() + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByOrderStatusAndUserId(OrderStatus orderStatus, Long userId, Pageable pageable)  {
        log.info("[OrderService:getAllOrdersByOrderStatusAndUserId] Start getAllOrdersByOrderStatusAndUserId");
        Page<Order> orders = orderRepository.findAllByStatusAndUserId(orderStatus, userId, pageable);
        log.info("[OrderService:getAllOrdersByOrderStatusAndUserId] Orders found: {}", orders);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_after_' + #afterCreatedAt.toEpochSecond() + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Pageable pageable)  {
        log.info("[OrderService:getAllOrdersByCreatedAtAfter] Start getOrdersByCreatedAtAfter");
        Page<Order> orders = orderRepository.findAllByCreatedAtAfter(afterCreatedAt, pageable);
        log.info("[OrderService:getAllOrdersByCreatedAtAfter] Orders found: {}", orders);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "'orders_after_' + #afterCreatedAt.toEpochSecond() + '_user_' + #userId + '_page_' + #pageable.pageNumber")
    public OrderListResponse getAllOrdersByCreatedAtAfterAndUserId(OffsetDateTime afterCreatedAt, Long userId, Pageable pageable)  {
        log.info("[OrderService:getAllOrdersByCreatedAtAfterAndUserId] Start getOrdersByCreatedAtAfterAndUserId");
        Page<Order> orders = orderRepository.findAllByCreatedAtAfterAndUserId(afterCreatedAt, userId, pageable);
        log.info("[OrderService:getOrdersByCreatedAtAfterAndUserId] Orders found: {}", orders);
        return orderMapper.toResponses(orders);
    }

    //TODO Вынести в отдельный сервис
    @Transactional
    @Scheduled(fixedRateString = "${scheduling.process-create-order-rate}")
    public void processCreateOrderEvent() {
        //TODO N+1
        log.info("[OrderService:processCreateOrderEvent] Start processCreateOrderEvent");
        List<CreateOrderEventOutbox> createOrderEventOutboxList = orderOutboxRepository.findTop100ByProcessedIsFalse();
        log.info("[OrderService:processCreateOrderEvent] order event outbox list found: {}", createOrderEventOutboxList);
        for (CreateOrderEventOutbox createOrderEventOutbox : createOrderEventOutboxList){
            try{
                log.info("[OrderService:processCreateOrderEvent] process order event outbox: {}", createOrderEventOutbox);
                OrderCreatedEvent event = new OrderCreatedEvent();
                event.setUserId(createOrderEventOutbox.getOrder().getUserId());
                event.setOrderId(createOrderEventOutbox.getOrder().getId());
                event.setItems(orderMapper.toResponses(createOrderEventOutbox.getOrder().getItems()));

                orderCreatedEventKafkaTemplate.send(orderCreatedTopic, event).get();
                log.info("[OrderService:processCreateOrderEvent] send order created event to message broker: {}", event);

                orderOutboxRepository.markAsProcessed(createOrderEventOutbox.getId());
                log.info("[OrderService:processCreateOrderEvent] mark order event outbox as processed");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("[OrderService:processCreateOrderEvent] Thread was interrupted while processing outbox with id: {}", createOrderEventOutbox.getId(), e);
                break;
            } catch (ExecutionException e) {
                log.error("[OrderService:processCreateOrderEvent] Failed to process outbox with id: {}", createOrderEventOutbox.getId(), e);
            }
        }
    }
}
