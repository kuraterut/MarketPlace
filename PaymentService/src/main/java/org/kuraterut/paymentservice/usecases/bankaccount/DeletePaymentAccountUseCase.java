package org.kuraterut.paymentservice.usecases.bankaccount;

public interface DeletePaymentAccountUseCase {
    void deleteBankAccountById(Long id, Long userId);
    void deleteBankAccountsByUserId(Long userId);
}
