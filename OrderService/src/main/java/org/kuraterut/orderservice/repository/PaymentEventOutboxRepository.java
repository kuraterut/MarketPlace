package org.kuraterut.orderservice.repository;

import org.kuraterut.orderservice.model.event.outbox.PaymentEventOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentEventOutboxRepository extends JpaRepository<PaymentEventOutbox, Long> {
    List<PaymentEventOutbox> findTop100ByProcessedIsFalse();
}
