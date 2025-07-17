package org.kuraterut.bankaccount.usecases.bankaccount;

import org.kuraterut.bankaccount.dto.request.CreateBankAccountRequest;
import org.kuraterut.bankaccount.dto.response.BankAccountResponse;

public interface CreateBankAccountUseCase {
    BankAccountResponse createBankAccount(CreateBankAccountRequest request, Long userId);
}
