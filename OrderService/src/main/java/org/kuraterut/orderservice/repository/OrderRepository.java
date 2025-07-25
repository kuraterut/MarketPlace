package org.kuraterut.orderservice.repository;

import org.kuraterut.orderservice.model.entity.Order;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByUserId(Long userId, Pageable pageable);
    Page<Order> findAllByStatus(OrderStatus orderStatus, Pageable pageable);
    Page<Order> findAllByStatusAndUserId(OrderStatus orderStatus, Long userId, Pageable pageable);
    Page<Order> findAllByCreatedAtAfter(OffsetDateTime createdAt, Pageable pageable);
    Page<Order> findAllByCreatedAtAfterAndUserId(OffsetDateTime createdAt, Long userId, Pageable pageable);
}
