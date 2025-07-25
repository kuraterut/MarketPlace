package org.kuraterut.productservice.usecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.support.Acknowledgment;

import java.util.concurrent.ExecutionException;

public interface OrderCreatedEventUseCase {
    void listenOrderCreated(String message, Acknowledgment ack);
    void processOrderCreatedEvent() throws JsonProcessingException, ExecutionException, InterruptedException;
}
