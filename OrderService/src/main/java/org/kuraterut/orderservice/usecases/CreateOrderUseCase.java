package org.kuraterut.orderservice.usecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderResponse;

public interface CreateOrderUseCase {
    OrderResponse createOrder(CreateOrderRequest request, Long userId) throws JsonProcessingException;
}
