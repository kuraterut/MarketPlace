package org.kuraterut.productservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldFailedEvent {
    private Long orderId;
    private List<ProductHoldItemFailed> items;
}
