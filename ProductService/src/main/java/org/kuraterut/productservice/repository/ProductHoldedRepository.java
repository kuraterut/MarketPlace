package org.kuraterut.productservice.repository;

import org.kuraterut.productservice.model.entity.ProductHolded;
import org.kuraterut.productservice.model.utils.ProductHoldedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductHoldedRepository extends JpaRepository<ProductHolded, Long> {
    List<ProductHolded> findAllByOrderId(Long orderId);
    List<ProductHolded> findTop100ByStatus(ProductHoldedStatus status);
    void deleteAllByStatus(ProductHoldedStatus status);

    @Modifying
    @Query("UPDATE ProductHolded ph SET ph.status = :status WHERE ph.orderId = :orderId")
    void updateStatusByOrderId(@Param("orderId") Long orderId, @Param("status") ProductHoldedStatus status);
}
