package org.kuraterut.paymentservice.repository;

import org.kuraterut.paymentservice.model.event.outbox.PaymentResultEventOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentResultOutboxRepository extends JpaRepository<PaymentResultEventOutbox, Long> {
    List<PaymentResultEventOutbox> findTop100ByProcessedIsFalse();
}
