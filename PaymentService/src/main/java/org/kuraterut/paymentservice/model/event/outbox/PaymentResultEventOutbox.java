package org.kuraterut.paymentservice.model.event.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.paymentservice.model.utils.PaymentResult;

@Entity
@Table(name = "payment_result_outbox")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultEventOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_result_event_outbox_seq")
    @SequenceGenerator(name = "payment_result_event_outbox_seq", sequenceName = "payment_result_event_outbox_seq", allocationSize = 1)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentResult result;

    private boolean processed;
}
