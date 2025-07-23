package org.kuraterut.orderservice.usecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.support.Acknowledgment;

import java.util.concurrent.ExecutionException;

public interface PaymentResultProcessUseCase {
    void listenPaymentResult(String message, Acknowledgment ack) throws JsonProcessingException;
    void processPaymentResult();
    void processProductHoldRemoveEvent() throws ExecutionException, InterruptedException;
}
