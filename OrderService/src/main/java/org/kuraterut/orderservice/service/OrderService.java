package org.kuraterut.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.dto.CreateOrderRequest;
import org.kuraterut.orderservice.dto.OrderResponse;
import org.kuraterut.orderservice.mapper.OrderMapper;
import org.kuraterut.orderservice.model.Order;
import org.kuraterut.orderservice.model.OrderItem;
import org.kuraterut.orderservice.model.OrderOutbox;
import org.kuraterut.orderservice.model.OrderStatus;
import org.kuraterut.orderservice.model.event.OrderCreatedEvent;
import org.kuraterut.orderservice.repository.OrderItemRepository;
import org.kuraterut.orderservice.repository.OrderOutboxRepository;
import org.kuraterut.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    //TODO Usecases
    private final OrderRepository orderRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderMapper orderMapper;

    private final KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate;


    @Value("${kafka-topics.order-created}")
    private String orderCreatedTopic;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) throws JsonProcessingException {
        Order order = orderMapper.toEntity(request, userId);

        order.setStatus(OrderStatus.CREATED);
        order = orderRepository.save(order);

        OrderOutbox orderOutbox = orderMapper.toOutbox(order);
        orderOutboxRepository.save(orderOutbox);

        return orderMapper.toResponse(order);
    }

    public OrderResponse getOrder(Long orderId) throws JsonProcessingException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return orderMapper.toResponse(order);
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
