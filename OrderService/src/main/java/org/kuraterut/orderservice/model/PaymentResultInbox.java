package org.kuraterut.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.event.PaymentResult;

@Entity
@Table(name = "payment_result_inbox")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultInbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private PaymentResult result;

    private boolean processed;
}
