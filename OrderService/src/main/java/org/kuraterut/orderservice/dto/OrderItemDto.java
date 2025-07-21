package org.kuraterut.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    //TODO Добавить Message везде.

    @NotNull
    private Long productId;
    @Min(1)
    private Long quantity;
}
