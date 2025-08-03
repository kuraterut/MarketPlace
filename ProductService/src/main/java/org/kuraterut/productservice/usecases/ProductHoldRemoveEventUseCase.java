package org.kuraterut.productservice.usecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.kafka.support.Acknowledgment;

public interface ProductHoldRemoveEventUseCase {
    void listenProductHoldRemoveEvent(String message, Acknowledgment ack) throws JsonProcessingException;
}
