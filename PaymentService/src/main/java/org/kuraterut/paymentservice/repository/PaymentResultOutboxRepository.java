package org.kuraterut.paymentservice.repository;

import org.kuraterut.paymentservice.model.event.PaymentResultOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentResultOutboxRepository extends JpaRepository<PaymentResultOutbox, Long> {
    List<PaymentResultOutbox> findTop100ByProcessedIsFalse();
}
