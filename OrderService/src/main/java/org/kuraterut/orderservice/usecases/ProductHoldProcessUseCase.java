package org.kuraterut.orderservice.usecases;

import org.springframework.kafka.support.Acknowledgment;

public interface ProductHoldProcessUseCase {
    void listenProductHoldFailed(String message, Acknowledgment ack);
    void listenProductHoldSuccess(String message, Acknowledgment ack);
}
