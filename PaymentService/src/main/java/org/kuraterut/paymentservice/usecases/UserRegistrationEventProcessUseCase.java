package org.kuraterut.paymentservice.usecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.support.Acknowledgment;

public interface UserRegistrationEventProcessUseCase {
    void listenUserRegistrationEvent(String message, Acknowledgment ack) throws JsonProcessingException;
}
