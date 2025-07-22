package org.kuraterut.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.OrderItem;
import org.kuraterut.orderservice.model.OrderStatus;
import org.kuraterut.orderservice.model.event.ProductHoldItemFailed;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<OrderItemDto> items;
    private List<ProductHoldItemFailed> details;
}
