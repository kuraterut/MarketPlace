package org.kuraterut.paymentservice.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_accounts")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaymentAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_account_seq")
    @SequenceGenerator(name = "payment_account_seq", sequenceName = "payment_account_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Version
    private Long version;
}
