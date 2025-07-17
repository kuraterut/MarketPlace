package org.kuraterut.bankaccount.repository;

import org.kuraterut.bankaccount.model.BankAccount;
import org.kuraterut.bankaccount.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findBankAccountByUserId(Long userId);
    List<BankAccount> findBankAccountByCurrencyAndUserId(Currency currency, Long userId);
    List<BankAccount> findBankAccountByActiveAndUserId(Boolean isActive, Long userId);
    List<BankAccount> findBankAccountByBalanceBetweenAndUserId(BigDecimal min, BigDecimal max, Long userId);
    boolean existsBankAccountByIdAndUserId(Long id, Long userId);
    @Modifying
    @Query("UPDATE BankAccount a SET a.balance = a.balance + :amount WHERE a.id = :id")
    int depositBankAccountById(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE BankAccount a SET a.balance = a.balance - :amount WHERE a.id = :id")
    int withdrawBankAccountById(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE BankAccount a SET a.active = TRUE WHERE a.id = :id")
    int activateBankAccountById(Long id);

    @Modifying
    @Query("UPDATE BankAccount a SET a.active = FALSE WHERE a.id = :id")
    int deactivateBankAccountById(Long id);

    @Query("SELECT COUNT(a) FROM BankAccount a WHERE a.userId = :userId AND a.balance != 0")
    int checkReadyForDeletingByUserId(@Param("userId") Long userId);

    void deleteAllByUserId(Long userId);

}
