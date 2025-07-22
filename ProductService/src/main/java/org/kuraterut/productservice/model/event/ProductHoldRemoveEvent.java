package org.kuraterut.productservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldRemoveEvent {
    private Long orderId;
    private ProductHoldRemoveEventDetails details;
}
