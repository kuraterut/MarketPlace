package org.kuraterut.paymentservice.model.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.paymentservice.model.utils.PaymentResult;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultEvent {
    private Long orderId;
    private PaymentResult result;
}
