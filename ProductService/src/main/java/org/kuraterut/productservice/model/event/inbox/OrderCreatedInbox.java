package org.kuraterut.productservice.model.event.inbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_created_inbox")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedInbox {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_created_inbox_seq")
    @SequenceGenerator(name = "order_created_inbox_seq", sequenceName = "order_created_inbox_seq", allocationSize = 1)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "json_items", joinColumns = @JoinColumn(name = "order_created_inbox_id"))
    @Column(name = "json_items")
    private List<String> jsonItems = new ArrayList<>();

    private boolean processed;
}
