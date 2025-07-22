package org.kuraterut.productservice.repository;

import org.kuraterut.productservice.model.OrderCreatedInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderCreatedInboxRepository extends JpaRepository<OrderCreatedInbox, Long> {
    List<OrderCreatedInbox> findTop100ByProcessedIsFalse();
}
