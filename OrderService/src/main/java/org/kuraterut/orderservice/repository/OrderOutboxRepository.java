package org.kuraterut.orderservice.repository;

import org.kuraterut.orderservice.model.event.outbox.CreateOrderEventOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderOutboxRepository extends JpaRepository<CreateOrderEventOutbox, Long> {
    List<CreateOrderEventOutbox> findTop100ByProcessedIsFalse();

    @Modifying
    @Query("UPDATE CreateOrderEventOutbox o SET o.processed = true WHERE o.id = :id")
    void markAsProcessed(@Param("id") Long id);
}
