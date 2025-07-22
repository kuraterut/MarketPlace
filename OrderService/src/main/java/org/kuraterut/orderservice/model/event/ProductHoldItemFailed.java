package org.kuraterut.orderservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldItemFailed {
    private Long productId;
    private Long quantity;
    private ProductHoldItemFailedReason reason;

}
