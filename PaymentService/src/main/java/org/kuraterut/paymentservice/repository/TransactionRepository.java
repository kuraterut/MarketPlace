package org.kuraterut.paymentservice.repository;

import org.kuraterut.paymentservice.model.entity.Transaction;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findAllByAccountId(Long accountId, Pageable pageable);
    Optional<Transaction> findByIdAndAccountId(Long id, Long accountId);

    Page<Transaction> findAllByAccountIdAndAmountBetween(Long accountId, BigDecimal min, BigDecimal max, Pageable pageable);
    Page<Transaction> findAllByAmountBetween(BigDecimal min, BigDecimal max, Pageable pageable);

    Page<Transaction> findAllByAccountIdAndStatus(Long accountId, TransactionStatus status, Pageable pageable);
    Page<Transaction> findAllByStatus(TransactionStatus status, Pageable pageable);

    Page<Transaction> findAllByAccountIdAndType(Long accountId, TransactionType type, Pageable pageable);
    Page<Transaction> findAllByType(TransactionType type, Pageable pageable);

    Page<Transaction> findAllByAccountIdAndOrderId(Long accountId, Long orderId, Pageable pageable);
    Page<Transaction> findAllByOrderId(Long orderId, Pageable pageable);
}
