package org.kuraterut.orderservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.event.ProductHoldRemoveEventDetails;

@Entity
@Table(name = "product_hold_remove_event_outbox")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHoldRemoveEventOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductHoldRemoveEventDetails details;

    private boolean processed;
}
