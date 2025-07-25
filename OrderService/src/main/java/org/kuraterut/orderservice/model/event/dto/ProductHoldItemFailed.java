package org.kuraterut.orderservice.model.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.utils.ProductHoldItemFailedReason;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldItemFailed {
    private Long productId;
    private Long quantity;
    private ProductHoldItemFailedReason reason;

}
