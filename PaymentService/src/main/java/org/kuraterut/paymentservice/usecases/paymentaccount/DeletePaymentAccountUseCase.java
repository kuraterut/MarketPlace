package org.kuraterut.paymentservice.usecases.paymentaccount;

public interface DeletePaymentAccountUseCase {
    void deletePaymentAccountById(Long id);
    void deletePaymentAccountByUserId(Long userId);
}
