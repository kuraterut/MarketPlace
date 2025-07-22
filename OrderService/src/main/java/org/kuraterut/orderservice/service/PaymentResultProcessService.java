package org.kuraterut.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.model.Order;
import org.kuraterut.orderservice.model.OrderStatus;
import org.kuraterut.orderservice.model.PaymentResultInbox;
import org.kuraterut.orderservice.model.ProductHoldRemoveEventOutbox;
import org.kuraterut.orderservice.model.event.PaymentResultEvent;
import org.kuraterut.orderservice.model.event.ProductHoldRemoveEvent;
import org.kuraterut.orderservice.model.event.ProductHoldRemoveEventDetails;
import org.kuraterut.orderservice.repository.OrderRepository;
import org.kuraterut.orderservice.repository.PaymentResultInboxRepository;
import org.kuraterut.orderservice.repository.ProductHoldRemoveEventOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
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
public class PaymentResultProcessService {
    private final ObjectMapper objectMapper;
    private final PaymentResultInboxRepository paymentResultInboxRepository;
    private final ProductHoldRemoveEventOutboxRepository productHoldRemoveEventOutboxRepository;
    private final OrderRepository orderRepository;

    private final KafkaTemplate<String, ProductHoldRemoveEvent> productHoldRemoveEventKafkaTemplate;

    @Value("${kafka-topics.product-hold-remove}")
    private String productHoldRemoveTopic;

    @KafkaListener(topics = "${kafka-topics.payment-result}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenPaymentResult(String message, Acknowledgment ack) throws JsonProcessingException {
        PaymentResultEvent event = objectMapper.readValue(message, PaymentResultEvent.class);
        PaymentResultInbox inbox =  new PaymentResultInbox();
        inbox.setProcessed(false);
        inbox.setResult(event.getResult());
        inbox.setOrderId(event.getOrderId());
        paymentResultInboxRepository.save(inbox);
        ack.acknowledge();
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void processPaymentResult() {
        List<PaymentResultInbox> inboxes = paymentResultInboxRepository.findTop100ByProcessedIsFalse();
        for (PaymentResultInbox inbox : inboxes) {
            Order order = orderRepository.findById(inbox.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            ProductHoldRemoveEventOutbox outbox = new ProductHoldRemoveEventOutbox();
            switch (inbox.getResult()){
                case SUCCESS:
                    order.setStatus(OrderStatus.COMPLETED);
                    outbox.setDetails(ProductHoldRemoveEventDetails.TO_REMOVE);
                    outbox.setOrderId(order.getId());
                    outbox.setProcessed(false);
                    productHoldRemoveEventOutboxRepository.save(outbox);
                    break;
                case NOT_ENOUGH_MONEY:
                    order.setStatus(OrderStatus.PAYMENT_FAILED_NOT_ENOUGH_MONEY);
                    outbox.setDetails(ProductHoldRemoveEventDetails.TO_RETURN);
                    outbox.setOrderId(order.getId());
                    outbox.setProcessed(false);
                    productHoldRemoveEventOutboxRepository.save(outbox);
                    break;
                case NOT_FOUND:
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

        }
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void processProductHoldRemoveEvent() throws ExecutionException, InterruptedException {
        List<ProductHoldRemoveEventOutbox> outboxes = productHoldRemoveEventOutboxRepository.findTop100ByProcessedIsFalse();
        for (ProductHoldRemoveEventOutbox outbox : outboxes) {
            ProductHoldRemoveEvent event = new ProductHoldRemoveEvent();
            event.setOrderId(outbox.getOrderId());
            event.setDetails(outbox.getDetails());
            productHoldRemoveEventKafkaTemplate.send(productHoldRemoveTopic, event).get();
            outbox.setProcessed(true);
            productHoldRemoveEventOutboxRepository.save(outbox);
        }
    }

}
