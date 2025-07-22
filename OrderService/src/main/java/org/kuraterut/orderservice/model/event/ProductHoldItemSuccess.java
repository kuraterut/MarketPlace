package org.kuraterut.orderservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldItemSuccess {
    private Long productId;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
