package org.kuraterut.orderservice.usecases;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.Order;
import org.kuraterut.orderservice.model.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;

public interface GetOrderUseCase {
    List<OrderResponse> getAllOrders() throws JsonProcessingException;
    OrderResponse getOrderById(Long orderId) throws JsonProcessingException;
    List<OrderResponse> getAllOrdersByUserId(Long userId) throws JsonProcessingException;
    List<OrderResponse> getAllOrdersByOrderStatus(OrderStatus orderStatus) throws JsonProcessingException;
    List<OrderResponse> getAllOrdersByOrderStatus(OrderStatus orderStatus, Long userId) throws JsonProcessingException;
    List<OrderResponse> getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt) throws JsonProcessingException;
    List<OrderResponse> getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Long userId) throws JsonProcessingException;
}
