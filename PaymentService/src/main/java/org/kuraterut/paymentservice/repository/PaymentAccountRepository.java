package org.kuraterut.paymentservice.repository;

import org.kuraterut.paymentservice.model.entity.PaymentAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
    Optional<PaymentAccount> findByUserId(Long userId);
    Page<PaymentAccount> findAllPaymentAccountByActive(Boolean isActive, Pageable pageable);
    Page<PaymentAccount> findAllPaymentAccountByBalanceBetween(BigDecimal min, BigDecimal max, Pageable pageable);

    boolean existsByUserId(Long userId);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance + :amount WHERE a.userId = :userId")
    int depositPaymentAccountByUserId(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance - :amount WHERE a.userId = :userId")
    int withdrawPaymentAccountByUserId(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance - :amount WHERE a.userId = :userId AND a.balance >= :amount")
    int withdrawPaymentAccountIfAvailableByUserId(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.active = TRUE WHERE a.id = :id")
    int activatePaymentAccountById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.active = TRUE WHERE a.userId = :userId")
    int activatePaymentAccountByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.active = FALSE WHERE a.id = :id")
    int deactivatePaymentAccountById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.active = FALSE WHERE a.userId = :userId")
    int deactivatePaymentAccountByUserId(@Param("userId") Long userId);


}
