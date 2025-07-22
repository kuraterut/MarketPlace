package org.kuraterut.orderservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldSuccessEvent {
    private Long orderId;
    private List<ProductHoldItemSuccess> items;
}
