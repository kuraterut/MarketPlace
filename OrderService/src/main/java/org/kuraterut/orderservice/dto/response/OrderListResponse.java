package org.kuraterut.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.entity.Order;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderListResponse {
    List<OrderResponse> orders;
}
