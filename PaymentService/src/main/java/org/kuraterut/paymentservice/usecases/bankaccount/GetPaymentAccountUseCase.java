package org.kuraterut.paymentservice.usecases.bankaccount;

import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.model.Currency;

import java.math.BigDecimal;
import java.util.List;

public interface GetPaymentAccountUseCase {
    List<PaymentAccountResponse> getAllBankAccounts();
    PaymentAccountResponse getBankAccountById(Long id, Long userId);
    List<PaymentAccountResponse> getBankAccountsByUserId(Long userId);
    List<PaymentAccountResponse> getBankAccountsByCurrency(Currency currency, Long userId);
    List<PaymentAccountResponse> getBankAccountsByIsActive(boolean isActive, Long userId);
    List<PaymentAccountResponse> getBankAccountsByBalanceBetween(BigDecimal min, BigDecimal max, Long userId);
}
