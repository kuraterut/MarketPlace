package org.kuraterut.orderservice.repository;

import org.kuraterut.orderservice.model.Order;
import org.kuraterut.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);
    List<Order> findAllByStatus(OrderStatus orderStatus);
    List<Order> findAllByStatusAndUserId(OrderStatus orderStatus, Long userId);
    List<Order> findAllByCreatedAtAfter(OffsetDateTime createdAt);
    List<Order> findAllByCreatedAtAfterAndUserId(OffsetDateTime createdAt, Long userId);
}
