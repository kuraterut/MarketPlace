package org.kuraterut.paymentservice.repository;

import org.kuraterut.paymentservice.model.PaymentAccount;
import org.kuraterut.paymentservice.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, Long> {
    List<PaymentAccount> findPaymentAccountByUserId(Long userId);
    List<PaymentAccount> findPaymentAccountByCurrencyAndUserId(Currency currency, Long userId);
    List<PaymentAccount> findPaymentAccountByActiveAndUserId(Boolean isActive, Long userId);
    List<PaymentAccount> findPaymentAccountByBalanceBetweenAndUserId(BigDecimal min, BigDecimal max, Long userId);

    boolean existsByUserId(Long userId);
    boolean existsPaymentAccountByIdAndUserId(Long id, Long userId);
    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance + :amount WHERE a.id = :id")
    int depositPaymentAccountById(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance - :amount WHERE a.id = :id")
    int withdrawPaymentAccountById(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.balance = a.balance - :amount WHERE a.userId = :userId AND a.balance >= :amount")
    int withdrawPaymentAccountIfAvailableByUserId(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.active = TRUE WHERE a.id = :id")
    int activatePaymentAccountById(Long id);

    @Modifying
    @Query("UPDATE PaymentAccount a SET a.active = FALSE WHERE a.id = :id")
    int deactivatePaymentAccountById(Long id);

    @Query("SELECT COUNT(a) FROM PaymentAccount a WHERE a.userId = :userId AND a.balance != 0")
    int checkReadyForDeletingByUserId(@Param("userId") Long userId);

    void deleteAllByUserId(Long userId);

}
