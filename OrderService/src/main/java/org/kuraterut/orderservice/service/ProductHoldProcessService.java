package org.kuraterut.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.exception.model.OrderNotFoundException;
import org.kuraterut.orderservice.model.Order;
import org.kuraterut.orderservice.model.OrderStatus;
import org.kuraterut.orderservice.model.PaymentEventOutbox;
import org.kuraterut.orderservice.model.event.ProductHoldFailedEvent;
import org.kuraterut.orderservice.model.event.ProductHoldItemFailed;
import org.kuraterut.orderservice.model.event.ProductHoldItemSuccess;
import org.kuraterut.orderservice.model.event.ProductHoldSuccessEvent;
import org.kuraterut.orderservice.repository.OrderRepository;
import org.kuraterut.orderservice.repository.PaymentEventOutboxRepository;
import org.kuraterut.orderservice.usecases.ProductHoldProcessUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductHoldProcessService implements ProductHoldProcessUseCase {
    private final OrderRepository orderRepository;
    private final PaymentEventOutboxRepository paymentEventOutboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @KafkaListener(topics = "${kafka-topics.product-hold-failed}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenProductHoldFailed(String message, Acknowledgment ack){
        try{
            ProductHoldFailedEvent event = objectMapper.readValue(message, ProductHoldFailedEvent.class);
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found by id: " + event.getOrderId()));
            List<String> details = new ArrayList<>();
            for(ProductHoldItemFailed item : event.getItems()){
                details.add(objectMapper.writeValueAsString(item));
            }
            order.setDetails(details);
            order.setStatus(OrderStatus.PRODUCT_RESERVATION_FAILED);

            orderRepository.save(order);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    @KafkaListener(topics = "${kafka-topics.product-hold-success}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenProductHoldSuccess(String message, Acknowledgment ack){
        try{
            ProductHoldSuccessEvent event = objectMapper.readValue(message, ProductHoldSuccessEvent.class);
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException("Order not found by id: " + event.getOrderId()));

            PaymentEventOutbox outbox = new PaymentEventOutbox();
            outbox.setOrderId(event.getOrderId());
            outbox.setUserId(order.getUserId());
            BigDecimal totalAmount = BigDecimal.ZERO;
            for(ProductHoldItemSuccess item : event.getItems()){
                totalAmount = totalAmount.add(item.getTotalPrice());
            }
            outbox.setAmount(totalAmount);
            outbox.setProcessed(false);
            paymentEventOutboxRepository.save(outbox);

            order.setStatus(OrderStatus.PENDING_PAYMENT);
            orderRepository.save(order);
            ack.acknowledge();

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
