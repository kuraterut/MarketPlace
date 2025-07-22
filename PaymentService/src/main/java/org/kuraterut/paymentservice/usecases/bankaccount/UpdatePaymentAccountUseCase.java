package org.kuraterut.paymentservice.usecases.bankaccount;

import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;

import java.math.BigDecimal;

public interface UpdatePaymentAccountUseCase {
    PaymentAccountResponse depositBankAccount(Long id, BigDecimal amount, Long userId);
    PaymentAccountResponse withdrawBankAccount(Long id, BigDecimal amount, Long userId);
    PaymentAccountResponse activateBankAccount(Long id, Long userId);
    PaymentAccountResponse deactivateBankAccount(Long id, Long userId);
}
