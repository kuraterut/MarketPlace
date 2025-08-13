package org.kuraterut.orderservice.model.event.inbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.utils.PaymentResult;

@Entity
@Table(name = "payment_result_inbox")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultInbox {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_result_inbox_seq")
    @SequenceGenerator(name = "payment_result_inbox_seq", sequenceName = "payment_result_inbox_seq", allocationSize = 1)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentResult result;

    private boolean processed;
}
