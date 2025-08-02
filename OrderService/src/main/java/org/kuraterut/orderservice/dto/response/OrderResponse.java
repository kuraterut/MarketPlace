package org.kuraterut.orderservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.event.dto.OrderItemDto;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.kuraterut.orderservice.model.event.dto.ProductHoldItemFailed;

import java.time.OffsetDateTime;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response with order info")
public class OrderResponse {
    @Schema(description = "Order ID", example = "1")
    private Long id;
    @Schema(description = "User ID, who created order", example = "1")
    private Long userId;
    @Schema(description = "Order status", example = "CREATED")
    private OrderStatus status;
    @Schema(description = "Creating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String createdAt;
    @Schema(description = "Updating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String updatedAt;
    @Schema(description = "Order items list (product ID with quantity)", example = "[{1, 3}, {2, 5}]")
    private List<OrderItemDto> items;
    @Schema(description = "Order failing details", example = "[{1, 2, NOT_FOUND}]")
    private List<ProductHoldItemFailed> details;
}
