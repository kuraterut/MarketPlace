package org.kuraterut.orderservice.usecases;

import org.kuraterut.orderservice.dto.response.OrderListResponse;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface GetOrderUseCase {
    OrderListResponse getAllOrders(Pageable pageable) ;
    OrderResponse getOrderById(Long orderId);
    OrderListResponse getAllOrdersByUserId(Long userId, Pageable pageable);
    OrderListResponse getAllOrdersByOrderStatus(OrderStatus orderStatus, Pageable pageable);
    OrderListResponse getAllOrdersByOrderStatus(OrderStatus orderStatus, Long userId, Pageable pageable);
    OrderListResponse getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Pageable pageable);
    OrderListResponse getAllOrdersByCreatedAtAfter(OffsetDateTime afterCreatedAt, Long userId, Pageable pageable);
}
