package org.kuraterut.orderservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.event.dto.OrderItemDto;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Create order request")
public class CreateOrderRequest {
    @NotEmpty(message = "Items list cannot be empty")
    @Schema(description = "Order items list (product ID with quantity)",
            example = "[{1, 3}, {2, 5}]", requiredMode = REQUIRED)
    private List<OrderItemDto> items;
}
