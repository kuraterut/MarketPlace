package org.kuraterut.paymentservice.repository;

import org.kuraterut.paymentservice.model.event.PaymentEventInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentEventInboxRepository extends JpaRepository<PaymentEventInbox, Long> {
    List<PaymentEventInbox> findTop100ByProcessedIsFalse();
}
