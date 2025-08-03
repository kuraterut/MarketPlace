package org.kuraterut.orderservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.event.dto.ProductHoldItemFailed;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldFailedEvent {
    private Long orderId;
    private List<ProductHoldItemFailed> items;
}
