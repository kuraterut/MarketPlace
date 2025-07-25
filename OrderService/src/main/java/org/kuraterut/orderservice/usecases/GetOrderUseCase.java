package org.kuraterut.orderservice.usecases;

import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface GetOrderUseCase {
    Page<OrderResponse> getAllOrders(Pageable pageable) ;
    OrderResponse getOrderById(Long orderId);
    Page<OrderResponse> getAllOrdersByUserId(Long userId, Pageable pageable);
    Page<OrderResponse> getAllOrdersByOrderStatus(OrderStatus orderStatus, Pageable pageable);
    Page<OrderResponse> getAllOrdersByOrderStatus(OrderStatus orderStatus, Long userId, Pageable pageable);
    Page<OrderResponse> getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Pageable pageable);
    Page<OrderResponse> getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Long userId, Pageable pageable);
}
