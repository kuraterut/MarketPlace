package org.kuraterut.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.exception.model.OrderNotFoundException;
import org.kuraterut.orderservice.model.entity.Order;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.kuraterut.orderservice.model.event.outbox.PaymentEventOutbox;
import org.kuraterut.orderservice.model.event.ProductHoldFailedEvent;
import org.kuraterut.orderservice.model.event.dto.ProductHoldItemFailed;
import org.kuraterut.orderservice.model.event.dto.ProductHoldItemSuccess;
import org.kuraterut.orderservice.model.event.ProductHoldSuccessEvent;
import org.kuraterut.orderservice.repository.OrderRepository;
import org.kuraterut.orderservice.repository.PaymentEventOutboxRepository;
import org.kuraterut.orderservice.usecases.ProductHoldProcessUseCase;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
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
@CacheConfig(cacheNames = "orders")
public class ProductHoldProcessService implements ProductHoldProcessUseCase {
    private final OrderRepository orderRepository;
    private final PaymentEventOutboxRepository paymentEventOutboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @KafkaListener(topics = "${kafka-topics.product-hold-failed}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    @CacheEvict(allEntries = true)
    public void listenProductHoldFailed(String message, Acknowledgment ack){
        try{
            log.info("[ProductHoldProcessService:listenProductHoldFailed] Start listenProductHoldFailed");
            ProductHoldFailedEvent event = objectMapper.readValue(message, ProductHoldFailedEvent.class);
            log.info("[ProductHoldProcessService:listenProductHoldFailed] Event received: {}", event);
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> {
                        log.warn("[ProductHoldProcessService:listenProductHoldFailed] Order not found with id: {}", event.getOrderId());
                        return new OrderNotFoundException("Order not found by id: " + event.getOrderId());
                    });
            log.info("[ProductHoldProcessService:listenProductHoldFailed] Order found: {}", order);
            List<String> details = new ArrayList<>();
            for(ProductHoldItemFailed item : event.getItems()){
                details.add(objectMapper.writeValueAsString(item));
            }
            order.setDetails(details);
            order.setStatus(OrderStatus.PRODUCT_RESERVATION_FAILED);

            orderRepository.save(order);
            log.info("[ProductHoldProcessService:listenProductHoldFailed] Product Reservation Failed details saved: {}", order);
            ack.acknowledge();
            log.info("[ProductHoldProcessService:listenProductHoldFailed] Acknowledge");
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    @KafkaListener(topics = "${kafka-topics.product-hold-success}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    @CacheEvict(allEntries = true)
    public void listenProductHoldSuccess(String message, Acknowledgment ack){
        try{
            log.info("[ProductHoldProcessService:listenProductHoldSuccess] Start listenProductHoldSuccess");
            ProductHoldSuccessEvent event = objectMapper.readValue(message, ProductHoldSuccessEvent.class);
            log.info("[ProductHoldProcessService:listenProductHoldSuccess] Event received: {}", event);
            Order order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> {
                        log.warn("[ProductHoldProcessService:listenProductHoldSuccess] Order not found with id: {}", event.getOrderId());
                        return new OrderNotFoundException("Order not found by id: " + event.getOrderId());
                    });

            PaymentEventOutbox outbox = new PaymentEventOutbox();
            outbox.setOrderId(event.getOrderId());
            outbox.setUserId(order.getUserId());
            BigDecimal totalAmount = BigDecimal.ZERO;
            for(ProductHoldItemSuccess item : event.getItems()){
                totalAmount = totalAmount.add(item.getTotalPrice());
            }
            log.info("[ProductHoldProcessService:listenProductHoldSuccess] Total Amount calculated: {}", totalAmount);
            outbox.setAmount(totalAmount);
            outbox.setProcessed(false);
            paymentEventOutboxRepository.save(outbox);
            log.info("[ProductHoldProcessService:listenProductHoldSuccess] Outbox saved: {}", outbox);

            order.setStatus(OrderStatus.PENDING_PAYMENT);
            orderRepository.save(order);

            ack.acknowledge();
            log.info("[ProductHoldProcessService:listenProductHoldSuccess] Acknowledge");

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
