package org.kuraterut.bankaccount.usecases.bankaccount;

import org.kuraterut.bankaccount.dto.response.BankAccountResponse;

import java.math.BigDecimal;

public interface UpdateBankAccountUseCase {
    BankAccountResponse depositBankAccount(Long id, BigDecimal amount, Long userId);
    BankAccountResponse withdrawBankAccount(Long id, BigDecimal amount, Long userId);
    BankAccountResponse activateBankAccount(Long id, Long userId);
    BankAccountResponse deactivateBankAccount(Long id, Long userId);
}
