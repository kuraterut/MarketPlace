package org.kuraterut.bankaccount.usecases.bankaccount;

import org.kuraterut.bankaccount.dto.response.BankAccountResponse;
import org.kuraterut.bankaccount.model.Currency;

import java.math.BigDecimal;
import java.util.List;

public interface GetBankAccountUseCase {
    List<BankAccountResponse> getAllBankAccounts();
    BankAccountResponse getBankAccountById(Long id, Long userId);
    List<BankAccountResponse> getBankAccountsByUserId(Long userId);
    List<BankAccountResponse> getBankAccountsByCurrency(Currency currency, Long userId);
    List<BankAccountResponse> getBankAccountsByIsActive(boolean isActive, Long userId);
    List<BankAccountResponse> getBankAccountsByBalanceBetween(BigDecimal min, BigDecimal max, Long userId);
}
