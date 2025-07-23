package org.kuraterut.paymentservice.usecases.paymentaccount;

import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;

import java.math.BigDecimal;
import java.util.List;

public interface GetPaymentAccountUseCase {
    List<PaymentAccountResponse> getAllPaymentAccounts();
    PaymentAccountResponse getPaymentAccountById(Long id);
    PaymentAccountResponse getPaymentAccountByUserId(Long userId);
    List<PaymentAccountResponse> getPaymentAccountsByIsActive(boolean isActive);
    List<PaymentAccountResponse> getPaymentAccountsByBalanceBetween(BigDecimal min, BigDecimal max);
}
