package org.kuraterut.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.exception.model.OrderNotFoundException;
import org.kuraterut.orderservice.model.entity.Order;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.kuraterut.orderservice.model.event.inbox.PaymentResultInbox;
import org.kuraterut.orderservice.model.event.outbox.ProductHoldRemoveEventOutbox;
import org.kuraterut.orderservice.model.event.PaymentResultEvent;
import org.kuraterut.orderservice.model.event.ProductHoldRemoveEvent;
import org.kuraterut.orderservice.model.utils.ProductHoldRemoveEventDetails;
import org.kuraterut.orderservice.repository.OrderRepository;
import org.kuraterut.orderservice.repository.PaymentResultInboxRepository;
import org.kuraterut.orderservice.repository.ProductHoldRemoveEventOutboxRepository;
import org.kuraterut.orderservice.usecases.PaymentResultProcessUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "orders")
public class PaymentResultProcessService implements PaymentResultProcessUseCase {
    private final ObjectMapper objectMapper;
    private final PaymentResultInboxRepository paymentResultInboxRepository;
    private final ProductHoldRemoveEventOutboxRepository productHoldRemoveEventOutboxRepository;
    private final OrderRepository orderRepository;

    private final KafkaTemplate<String, ProductHoldRemoveEvent> productHoldRemoveEventKafkaTemplate;

    @Value("${kafka-topics.product-hold-remove}")
    private String productHoldRemoveTopic;

    @Override
    @KafkaListener(topics = "${kafka-topics.payment-result}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenPaymentResult(String message, Acknowledgment ack) throws JsonProcessingException {
        log.info("[PaymentResultProcessService:listenPaymentResult] Start listenPaymentResult");
        PaymentResultEvent event = objectMapper.readValue(message, PaymentResultEvent.class);
        log.info("[PaymentResultProcessService:listenPaymentResult] Received event {}", event);
        PaymentResultInbox inbox =  new PaymentResultInbox();
        inbox.setProcessed(false);
        inbox.setResult(event.getResult());
        inbox.setOrderId(event.getOrderId());
        paymentResultInboxRepository.save(inbox);
        log.info("[PaymentResultProcessService:listenPaymentResult] Payment Result Inbox created and saved");
        ack.acknowledge();
        log.info("[PaymentResultProcessService:listenPaymentResult] Acknowledged");
    }

    @Override
    @Scheduled(fixedRateString = "${scheduling.process-payment-result-rate}")
    @Transactional
    @CacheEvict(allEntries = true)
    public void processPaymentResult() {
        log.info("[PaymentResultProcessService:processPaymentResult] Start processPaymentResult");
        List<PaymentResultInbox> inboxes = paymentResultInboxRepository.findTop100ByProcessedIsFalse();
        log.info("[PaymentResultProcessService:processPaymentResult] Processing inboxes");
        for (PaymentResultInbox inbox : inboxes) {
            log.info("[PaymentResultProcessService:processPaymentResult] Processing inbox {}", inbox);
            Order order = orderRepository.findById(inbox.getOrderId())
                    .orElseThrow(() -> {
                        log.warn("[PaymentResultProcessService:processPaymentResult] Order not found with id {}", inbox.getOrderId());
                        return new OrderNotFoundException("Order not found by id: " + inbox.getOrderId());
                    });
            log.info("[PaymentResultProcessService:processPaymentResult] Order found: {}", order);
            ProductHoldRemoveEventOutbox outbox = new ProductHoldRemoveEventOutbox();
            switch (inbox.getResult()){
                case SUCCESS:
                    log.info("[PaymentResultProcessService:processPaymentResult] Successfully processed order");
                    order.setStatus(OrderStatus.COMPLETED);
                    outbox.setDetails(ProductHoldRemoveEventDetails.TO_REMOVE);
                    outbox.setOrderId(order.getId());
                    outbox.setProcessed(false);
                    productHoldRemoveEventOutboxRepository.save(outbox);
                    break;
                case NOT_ENOUGH_MONEY:
                    log.warn("[PaymentResultProcessService:processPaymentResult] Not Enough money for order");
                    order.setStatus(OrderStatus.PAYMENT_FAILED_NOT_ENOUGH_MONEY);
                    outbox.setDetails(ProductHoldRemoveEventDetails.TO_RETURN);
                    outbox.setOrderId(order.getId());
                    outbox.setProcessed(false);
                    productHoldRemoveEventOutboxRepository.save(outbox);
                    break;
                case NOT_FOUND:
                    log.warn("[PaymentResultProcessService:processPaymentResult] Payment Account not found");
                    order.setStatus(OrderStatus.PAYMENT_FAILED_NOT_FOUND);
                    outbox.setDetails(ProductHoldRemoveEventDetails.TO_RETURN);
                    outbox.setOrderId(order.getId());
                    outbox.setProcessed(false);
                    productHoldRemoveEventOutboxRepository.save(outbox);
                    break;
            }
            orderRepository.save(order);
            inbox.setProcessed(true);
            paymentResultInboxRepository.save(inbox);
            log.info("[PaymentResultProcessService:processPaymentResult] Payment Result Inbox processed");
        }
    }

    @Override
    @Scheduled(fixedRateString = "${scheduling.process-product-hold-remove-rate}")
    @Transactional
    public void processProductHoldRemoveEvent() throws ExecutionException, InterruptedException {
        log.info("[PaymentResultProcessService:processProductHoldRemoveEvent] Start processProductHoldRemoveEvent");
        List<ProductHoldRemoveEventOutbox> outboxes = productHoldRemoveEventOutboxRepository.findTop100ByProcessedIsFalse();
        log.info("[PaymentResultProcessService:processProductHoldRemoveEvent] Processing product hold remove events");
        for (ProductHoldRemoveEventOutbox outbox : outboxes) {
            log.info("[PaymentResultProcessService:processProductHoldRemoveEvent] Processing outbox {}", outbox);
            ProductHoldRemoveEvent event = new ProductHoldRemoveEvent();
            event.setOrderId(outbox.getOrderId());
            event.setDetails(outbox.getDetails());
            productHoldRemoveEventKafkaTemplate.send(productHoldRemoveTopic, event).get();
            log.info("[PaymentResultProcessService:processProductHoldRemoveEvent] product hold remove event was " +
                    "sent to message broker");
            outbox.setProcessed(true);
            productHoldRemoveEventOutboxRepository.save(outbox);
            log.info("[PaymentResultProcessService:processProductHoldRemoveEvent] outbox processed");
        }
    }
}
