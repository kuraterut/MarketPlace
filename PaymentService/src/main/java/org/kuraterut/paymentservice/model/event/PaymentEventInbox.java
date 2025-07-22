package org.kuraterut.paymentservice.model.event;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_event_inbox")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEventInbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "processed", nullable = false)
    private boolean processed;
}
