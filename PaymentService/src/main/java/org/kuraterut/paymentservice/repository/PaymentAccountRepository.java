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

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
    Page<PaymentAccount> findAllPaymentAccountByActive(Boolean isActive, Pageable pageable);
    Page<PaymentAccount> findAllPaymentAccountByBalanceBetween(BigDecimal min, BigDecimal max, Pageable pageable);


    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance + :amount WHERE a.id = :id")
    int depositPaymentAccount(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance - :amount WHERE a.id = :id")
    int withdrawPaymentAccount(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance - :amount WHERE a.id = :id AND a.balance >= :amount")
    int withdrawPaymentAccountIfAvailable(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.active = TRUE WHERE a.id = :id")
    int activatePaymentAccount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.active = FALSE WHERE a.id = :id")
    int deactivatePaymentAccount(@Param("id") Long id);


}
