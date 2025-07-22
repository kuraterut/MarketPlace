package org.kuraterut.paymentservice.usecases.bankaccount;

import org.kuraterut.paymentservice.dto.request.CreatePaymentAccountRequest;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;

public interface CreatePaymentAccountUseCase {
    PaymentAccountResponse createBankAccount(CreatePaymentAccountRequest request, Long userId);
}
