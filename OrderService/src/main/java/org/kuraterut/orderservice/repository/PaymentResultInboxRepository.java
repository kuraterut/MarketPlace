package org.kuraterut.orderservice.repository;

import org.kuraterut.orderservice.model.event.inbox.PaymentResultInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentResultInboxRepository extends JpaRepository<PaymentResultInbox, Long> {
    List<PaymentResultInbox> findTop100ByProcessedIsFalse();
}
