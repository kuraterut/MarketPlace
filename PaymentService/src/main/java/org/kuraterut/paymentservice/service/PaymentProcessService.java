package org.kuraterut.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.paymentservice.model.PaymentAccount;
import org.kuraterut.paymentservice.model.Transaction;
import org.kuraterut.paymentservice.model.TransactionStatus;
import org.kuraterut.paymentservice.model.TransactionType;
import org.kuraterut.paymentservice.model.event.*;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.repository.PaymentEventInboxRepository;
import org.kuraterut.paymentservice.repository.PaymentResultOutboxRepository;
import org.kuraterut.paymentservice.repository.TransactionRepository;
import org.kuraterut.paymentservice.usecases.PaymentProcessUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
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
        PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
        PaymentEventInbox inbox = new PaymentEventInbox();
        inbox.setProcessed(false);
        inbox.setOrderId(event.getOrderId());
        inbox.setUserId(event.getUserId());
        inbox.setAmount(event.getAmount());
        paymentEventInboxRepository.save(inbox);
        ack.acknowledge();
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 3000)
    public void processPaymentEvent() {
        List<PaymentEventInbox> inboxes = paymentEventInboxRepository.findTop100ByProcessedIsFalse();
        for (PaymentEventInbox inbox : inboxes) {
            BigDecimal amount = inbox.getAmount();
            Long userId = inbox.getUserId();

            Optional<PaymentAccount> accountOpt = paymentAccountRepository.findByUserId(userId);

            if(accountOpt.isEmpty()){
                PaymentResultOutbox outbox = new PaymentResultOutbox();
                outbox.setProcessed(false);
                outbox.setOrderId(inbox.getOrderId());
                outbox.setResult(PaymentResult.NOT_FOUND);
                paymentResultOutboxRepository.save(outbox);
                inbox.setProcessed(true);
                paymentEventInboxRepository.save(inbox);
                continue;
            }
            PaymentAccount account = accountOpt.get();

            int updatedRows = paymentAccountRepository.withdrawPaymentAccountIfAvailableByUserId(userId, amount);
            PaymentResultOutbox outbox = new PaymentResultOutbox();
            outbox.setProcessed(false);
            outbox.setOrderId(inbox.getOrderId());

            Transaction transaction = new Transaction();
            transaction.setAmount(amount);
            transaction.setAccount(account);
            transaction.setType(TransactionType.PAYMENT);
            transaction.setOrderId(inbox.getOrderId());

            if(updatedRows == 0){
                outbox.setResult(PaymentResult.NOT_ENOUGH_MONEY);
                transaction.setStatus(TransactionStatus.FAILED);
            } else {
                outbox.setResult(PaymentResult.SUCCESS);
                transaction.setStatus(TransactionStatus.COMPLETED);
            }
            transactionRepository.save(transaction);
            paymentResultOutboxRepository.save(outbox);
            inbox.setProcessed(true);
            paymentEventInboxRepository.save(inbox);
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 3000)
    public void processPaymentResult() throws ExecutionException, InterruptedException {
        List<PaymentResultOutbox> outboxes = paymentResultOutboxRepository.findTop100ByProcessedIsFalse();
        for (PaymentResultOutbox outbox : outboxes) {
            PaymentResultEvent event = new PaymentResultEvent();
            event.setOrderId(outbox.getOrderId());
            event.setResult(outbox.getResult());
            paymentResultEventKafkaTemplate.send(paymentResultTopic, event).get();
            outbox.setProcessed(true);
            paymentResultOutboxRepository.save(outbox);
        }
    }

}
