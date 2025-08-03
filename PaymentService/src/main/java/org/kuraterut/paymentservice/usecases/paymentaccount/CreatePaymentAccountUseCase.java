package org.kuraterut.paymentservice.usecases.paymentaccount;

import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;

public interface CreatePaymentAccountUseCase {
    PaymentAccountResponse createPaymentAccount(Long userId);
}
