package org.kuraterut.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.model.event.outbox.PaymentEventOutbox;
import org.kuraterut.orderservice.model.event.PaymentEvent;
import org.kuraterut.orderservice.repository.PaymentEventOutboxRepository;
import org.kuraterut.orderservice.usecases.PaymentProcessUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessService implements PaymentProcessUseCase {
    private final PaymentEventOutboxRepository paymentEventOutboxRepository;
    private final KafkaTemplate<String, PaymentEvent> paymentEventKafkaTemplate;

    @Value("${kafka-topics.payment-request}")
    private String paymentRequestTopic;

    @Override
    @Scheduled(fixedRateString = "${scheduling.process-payment-event-rate}")
    @Transactional
    public void processPaymentEvents() throws ExecutionException, InterruptedException {
        log.info("[PaymentProcessService:processPaymentEvents] Start processPaymentEvents");
        List<PaymentEventOutbox> outboxes = paymentEventOutboxRepository.findTop100ByProcessedIsFalse();
        log.info("[PaymentProcessService:processPaymentEvents] Payment Event Outbox list found");
        for (PaymentEventOutbox outbox : outboxes) {
            log.info("[PaymentProcessService:processPaymentEvents] Processing outbox {}", outbox);
            PaymentEvent event = new PaymentEvent();
            event.setAmount(outbox.getAmount());
            event.setOrderId(outbox.getOrderId());
            event.setUserId(outbox.getUserId());

            paymentEventKafkaTemplate.send(paymentRequestTopic, event).get();
            log.info("[PaymentProcessService:processPaymentEvents] Send payment event to message broker {}", event);
            outbox.setProcessed(true);
            paymentEventOutboxRepository.save(outbox);
            log.info("[PaymentProcessService:processPaymentEvents] Out box processed");
        }
    }
}
