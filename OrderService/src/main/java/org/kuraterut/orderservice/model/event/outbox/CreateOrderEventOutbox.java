package org.kuraterut.orderservice.model.event.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.orderservice.model.entity.Order;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders_outbox")
public class CreateOrderEventOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "create_order_event_outbox_seq")
    @SequenceGenerator(name = "create_order_event_outbox_seq", sequenceName = "create_order_event_outbox_seq", allocationSize = 1)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private boolean processed;
}
