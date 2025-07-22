package org.kuraterut.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.orderservice.model.PaymentEventOutbox;
import org.kuraterut.orderservice.model.event.PaymentEvent;
import org.kuraterut.orderservice.repository.PaymentEventOutboxRepository;
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
public class PaymentProcessService {
    private final PaymentEventOutboxRepository paymentEventOutboxRepository;
    private final KafkaTemplate<String, PaymentEvent> paymentEventKafkaTemplate;

    @Value("${kafka-topics.payment-request}")
    private String paymentRequestTopic;

    @Scheduled(fixedRate = 2000)
    @Transactional
    public void processPaymentEvents() throws ExecutionException, InterruptedException {
        List<PaymentEventOutbox> outboxes = paymentEventOutboxRepository.findTop100ByProcessedIsFalse();
        for (PaymentEventOutbox outbox : outboxes) {
            PaymentEvent event = new PaymentEvent();
            event.setAmount(outbox.getAmount());
            event.setOrderId(outbox.getOrderId());
            event.setUserId(outbox.getUserId());

            paymentEventKafkaTemplate.send(paymentRequestTopic, event).get();
            outbox.setProcessed(true);
            paymentEventOutboxRepository.save(outbox);
        }
    }
}
