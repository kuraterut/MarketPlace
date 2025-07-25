package org.kuraterut.productservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.productservice.model.utils.ProductHoldedStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "product_holded")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductHolded {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_holded_seq")
    @SequenceGenerator(name = "product_holded_seq", sequenceName = "product_holded_seq", allocationSize = 1)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductHoldedStatus status;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;
}
