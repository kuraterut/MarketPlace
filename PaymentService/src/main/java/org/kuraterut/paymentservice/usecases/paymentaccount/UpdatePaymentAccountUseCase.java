package org.kuraterut.paymentservice.usecases.paymentaccount;

import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;

import java.math.BigDecimal;

public interface UpdatePaymentAccountUseCase {
    PaymentAccountResponse depositPaymentAccountByUserId(Long userId, BigDecimal amount);
    PaymentAccountResponse withdrawPaymentAccountByUserId(Long userId, BigDecimal amount);
    PaymentAccountResponse activatePaymentAccountByUserId(Long userId);
    PaymentAccountResponse deactivatePaymentAccountByUserId(Long userId);
}
