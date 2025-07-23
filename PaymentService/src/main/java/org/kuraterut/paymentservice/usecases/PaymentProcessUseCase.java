package org.kuraterut.paymentservice.usecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.support.Acknowledgment;

import java.util.concurrent.ExecutionException;

public interface PaymentProcessUseCase {
    void listenPaymentEvent(String message, Acknowledgment ack) throws JsonProcessingException;
    void processPaymentEvent();
    void processPaymentResult() throws ExecutionException, InterruptedException;
}
