package org.kuraterut.paymentservice.usecases.paymentaccount;

import org.kuraterut.paymentservice.dto.response.PaymentAccountListResponse;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface GetPaymentAccountUseCase {
    PaymentAccountListResponse getAllPaymentAccounts(Pageable pageable);
    PaymentAccountResponse getPaymentAccountById(Long id);
    PaymentAccountResponse getPaymentAccountByUserId(Long userId);
    PaymentAccountListResponse getPaymentAccountsByIsActive(boolean isActive, Pageable pageable);
    PaymentAccountListResponse getPaymentAccountsByBalanceBetween(BigDecimal min, BigDecimal max, Pageable pageable);
}
