package org.kuraterut.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.exception.model.OrderNotFoundException;
import org.kuraterut.orderservice.mapper.OrderMapper;
import org.kuraterut.orderservice.model.Order;
import org.kuraterut.orderservice.model.OrderOutbox;
import org.kuraterut.orderservice.model.OrderStatus;
import org.kuraterut.orderservice.model.event.OrderCreatedEvent;
import org.kuraterut.orderservice.repository.OrderOutboxRepository;
import org.kuraterut.orderservice.repository.OrderRepository;
import org.kuraterut.orderservice.usecases.CreateOrderUseCase;
import org.kuraterut.orderservice.usecases.GetOrderUseCase;
import org.springframework.beans.factory.annotation.Value;
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
public class OrderService implements CreateOrderUseCase, GetOrderUseCase{
    private final OrderRepository orderRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderMapper orderMapper;

    private final KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate;

    @Value("${kafka-topics.order-created}")
    private String orderCreatedTopic;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) throws JsonProcessingException {
        Order order = orderMapper.toEntity(request, userId);

        order.setStatus(OrderStatus.CREATED);
        order = orderRepository.save(order);

        OrderOutbox orderOutbox = orderMapper.toOutbox(order);
        orderOutboxRepository.save(orderOutbox);

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() throws JsonProcessingException {
        List<Order> orders = orderRepository.findAll();
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) throws JsonProcessingException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found by id: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersByUserId(Long userId) throws JsonProcessingException {
        List<Order> orders = orderRepository.findAllByUserId(userId);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersByOrderStatus(OrderStatus orderStatus) throws JsonProcessingException {
        List<Order> orders = orderRepository.findAllByStatus(orderStatus);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersByOrderStatus(OrderStatus orderStatus, Long userId) throws JsonProcessingException {
        List<Order> orders = orderRepository.findAllByStatusAndUserId(orderStatus, userId);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt) throws JsonProcessingException {
        List<Order> orders = orderRepository.findAllByCreatedAtAfter(afterCreatedAt);
        return orderMapper.toResponses(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Long userId) throws JsonProcessingException {
        List<Order> orders = orderRepository.findAllByCreatedAtAfterAndUserId(afterCreatedAt, userId);
        return orderMapper.toResponses(orders);
    }

    //TODO Настроить тайминги
    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void processCreateOrderEvent() {
        //TODO N+1
        //TODO Пагинация или лимиты
        List<OrderOutbox> orderOutboxList = orderOutboxRepository.findTop100ByProcessedIsFalse();
        for (OrderOutbox orderOutbox : orderOutboxList){
            try{
                OrderCreatedEvent event = new OrderCreatedEvent();
                event.setUserId(orderOutbox.getOrder().getUserId());
                event.setOrderId(orderOutbox.getOrder().getId());
                event.setItems(orderMapper.toResponses(orderOutbox.getOrder().getItems()));

                orderCreatedEventKafkaTemplate.send(orderCreatedTopic, event).get();

                orderOutboxRepository.markAsProcessed(orderOutbox.getId());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Failed to process outbox with id: {}", orderOutbox.getId(), e);
            }
        }
    }

}
