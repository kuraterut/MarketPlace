package org.kuraterut.orderservice.repository;

import org.kuraterut.orderservice.model.event.outbox.ProductHoldRemoveEventOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductHoldRemoveEventOutboxRepository extends JpaRepository<ProductHoldRemoveEventOutbox, Long> {
    List<ProductHoldRemoveEventOutbox> findTop100ByProcessedIsFalse();
}
