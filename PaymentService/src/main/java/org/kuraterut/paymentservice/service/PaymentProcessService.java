package org.kuraterut.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.paymentservice.model.entity.PaymentAccount;
import org.kuraterut.paymentservice.model.entity.Transaction;
import org.kuraterut.paymentservice.model.event.inbox.PaymentEventInbox;
import org.kuraterut.paymentservice.model.event.outbox.PaymentResultEventOutbox;
import org.kuraterut.paymentservice.model.utils.PaymentResult;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;
import org.kuraterut.paymentservice.model.event.*;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.repository.PaymentEventInboxRepository;
import org.kuraterut.paymentservice.repository.PaymentResultOutboxRepository;
import org.kuraterut.paymentservice.repository.TransactionRepository;
import org.kuraterut.paymentservice.usecases.eventprocessing.PaymentProcessUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
@CacheConfig
public class PaymentProcessService implements PaymentProcessUseCase {
    private final ObjectMapper objectMapper;
    private final PaymentAccountRepository paymentAccountRepository;
    private final PaymentEventInboxRepository paymentEventInboxRepository;
    private final PaymentResultOutboxRepository paymentResultOutboxRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, PaymentResultEvent> paymentResultEventKafkaTemplate;

    @Value("${kafka-topics.payment-result}")
    private String paymentResultTopic;

    @Override
    @KafkaListener(topics = "${kafka-topics.payment-request}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenPaymentEvent(String message, Acknowledgment ack) throws JsonProcessingException {
        log.info("[PaymentProcessService:listenPaymentEvent] Start listenPaymentEvent");
        PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
        log.info("[PaymentProcessService:listenPaymentEvent] Received event {}", event);
        PaymentEventInbox inbox = new PaymentEventInbox();
        inbox.setProcessed(false);
        inbox.setOrderId(event.getOrderId());
        inbox.setUserId(event.getUserId());
        inbox.setAmount(event.getAmount());
        paymentEventInboxRepository.save(inbox);
        log.info("[PaymentProcessService:listenPaymentEvent] PaymentEventInbox created and saved");
        ack.acknowledge();
        log.info("[PaymentProcessService:listenPaymentEvent] Acknowledged");
    }

    @Override
    @Transactional
    @Scheduled(fixedRateString = "${scheduling.process-payment-event-rate}")
    @Caching(evict = {
            @CacheEvict(cacheNames = "payment_accounts", allEntries = true),
            @CacheEvict(cacheNames = "transactions", allEntries = true)
    })
    public void processPaymentEvent() {
        log.info("[PaymentProcessService:processPaymentEvent] Start processPaymentEvent");
        List<PaymentEventInbox> inboxes = paymentEventInboxRepository.findTop100ByProcessedIsFalse();
        log.info("[PaymentProcessService:processPaymentEvent] Unprocessed PaymentEvent inboxes found");
        for (PaymentEventInbox inbox : inboxes) {
            log.info("[PaymentProcessService:processPaymentEvent] Process inbox event {}", inbox);
            BigDecimal amount = inbox.getAmount();
            Long userId = inbox.getUserId();

            log.info("[PaymentProcessService:processPaymentEvent] Try find Payment Account By User ID: {}", userId);
            Optional<PaymentAccount> accountOpt = paymentAccountRepository.findByUserId(userId);

            if(accountOpt.isEmpty()){
                log.warn("[PaymentProcessService:processPaymentEvent] Account not found By User ID: {}", userId);
                PaymentResultEventOutbox outbox = new PaymentResultEventOutbox();
                outbox.setProcessed(false);
                outbox.setOrderId(inbox.getOrderId());
                outbox.setResult(PaymentResult.NOT_FOUND);
                paymentResultOutboxRepository.save(outbox);
                inbox.setProcessed(true);
                paymentEventInboxRepository.save(inbox);
                log.info("[PaymentProcessService:processPaymentEvent] PaymentResultEventOutbox created and saved");
                continue;
            }
            PaymentAccount account = accountOpt.get();
            log.info("[PaymentProcessService:processPaymentEvent] Account found By User ID: {}", userId);
            log.info("[PaymentProcessService:processPaymentEvent] Try withdraw Payment Account If Available");
            int updatedRows = paymentAccountRepository.withdrawPaymentAccountIfAvailableByUserId(userId, amount);
            PaymentResultEventOutbox outbox = new PaymentResultEventOutbox();
            outbox.setProcessed(false);
            outbox.setOrderId(inbox.getOrderId());

            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setAccount(account);
            transaction.setType(TransactionType.PAYMENT);
            transaction.setOrderId(inbox.getOrderId());

            if(updatedRows == 0){
                log.warn("[PaymentProcessService:processPaymentEvent] Withdraw failed, Not Enough Money");
                outbox.setResult(PaymentResult.NOT_ENOUGH_MONEY);
                transaction.setStatus(TransactionStatus.FAILED);
            } else {
                log.info("[PaymentProcessService:processPaymentEvent] Withdraw successful");
                outbox.setResult(PaymentResult.SUCCESS);
                transaction.setStatus(TransactionStatus.COMPLETED);
            }
            transactionRepository.save(transaction);
            paymentResultOutboxRepository.save(outbox);
            inbox.setProcessed(true);
            paymentEventInboxRepository.save(inbox);
            log.info("[PaymentProcessService:processPaymentEvent] PaymentResultOutbox created and saved, Inbox processed");
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedRateString = "${scheduling.process-payment-result-rate}")
    public void processPaymentResult() throws ExecutionException, InterruptedException {
        log.info("[PaymentProcessService:processPaymentResult] Start processPaymentResult");
        List<PaymentResultEventOutbox> outboxes = paymentResultOutboxRepository.findTop100ByProcessedIsFalse();
        log.info("[PaymentProcessService:processPaymentResult] Unprocessed PaymentResultEventOutbox found");
        for (PaymentResultEventOutbox outbox : outboxes) {
            log.info("[PaymentProcessService:processPaymentResult] Process inbox event {}", outbox);
            PaymentResultEvent event = new PaymentResultEvent();
            event.setOrderId(outbox.getOrderId());
            event.setResult(outbox.getResult());
            paymentResultEventKafkaTemplate.send(paymentResultTopic, event).get();
            log.info("[PaymentProcessService:processPaymentResult] paymentResultEvent was sent to message broker");
            outbox.setProcessed(true);
            paymentResultOutboxRepository.save(outbox);
            log.info("[PaymentProcessService:processPaymentResult] PaymentResultOutbox processed");
        }
    }
}
