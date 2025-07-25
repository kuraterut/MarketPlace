package org.kuraterut.orderservice.model.event.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.utils.ProductHoldRemoveEventDetails;

@Entity
@Table(name = "product_hold_remove_event_outbox")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldRemoveEventOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_hold_remove_event_outbox_seq")
    @SequenceGenerator(name = "product_hold_remove_event_outbox_seq", sequenceName = "product_hold_remove_event_outbox_seq", allocationSize = 1)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductHoldRemoveEventDetails details;

    private boolean processed;
}
