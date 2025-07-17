package org.kuraterut.bankaccount.usecases.bankaccount;

import org.kuraterut.bankaccount.model.BankAccount;

public interface DeleteBankAccountUseCase {
    void deleteBankAccountById(Long id, Long userId);
    void deleteBankAccountsByUserId(Long userId);
}
