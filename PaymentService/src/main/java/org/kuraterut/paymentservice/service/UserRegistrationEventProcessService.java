package org.kuraterut.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.paymentservice.mapper.PaymentAccountMapper;
import org.kuraterut.paymentservice.model.entity.PaymentAccount;
import org.kuraterut.paymentservice.model.event.UserRegistrationEvent;
import org.kuraterut.paymentservice.repository.PaymentAccountRepository;
import org.kuraterut.paymentservice.usecases.eventprocessing.UserRegistrationEventProcessUseCase;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationEventProcessService implements UserRegistrationEventProcessUseCase {
    private final ObjectMapper objectMapper;
    private final PaymentAccountMapper paymentAccountMapper;
    private final PaymentAccountRepository paymentAccountRepository;

    @Override
    @KafkaListener(topics = "${kafka-topics.user-registration}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    @CacheEvict(cacheNames = "payment_accounts", allEntries = true)
    public void listenUserRegistrationEvent(String message, Acknowledgment ack) throws JsonProcessingException {
        log.info("[UserRegistrationEventProcessService:listenUserRegistrationEvent] Start listenUserRegistrationEvent");
        UserRegistrationEvent event = objectMapper.readValue(message, UserRegistrationEvent.class);
        log.info("[UserRegistrationEventProcessService:listenUserRegistrationEvent] Received user registration event: {}", event);
        if(!paymentAccountRepository.existsByUserId(event.getUserId())) {
            PaymentAccount account = paymentAccountMapper.toEntity(event.getUserId());
            paymentAccountRepository.save(account);
            log.info("[UserRegistrationEventProcessService:listenUserRegistrationEvent] Saved user account: {}", account);
        }
        ack.acknowledge();
        log.info("[UserRegistrationEventProcessService:listenUserRegistrationEvent] Acknowledged");
    }

}
