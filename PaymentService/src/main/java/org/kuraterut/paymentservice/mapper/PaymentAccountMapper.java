package org.kuraterut.paymentservice.mapper;

import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.model.PaymentAccount;
import org.kuraterut.paymentservice.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PaymentAccountMapper {
    public PaymentAccount toEntity(Long userId) {
        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setUserId(userId);
        paymentAccount.setActive(true);
        paymentAccount.setBalance(BigDecimal.ZERO);
        return paymentAccount;
    }

    public PaymentAccountResponse toResponse(PaymentAccount paymentAccount) {
        PaymentAccountResponse paymentAccountResponse = new PaymentAccountResponse();
        paymentAccountResponse.setId(paymentAccount.getId());
        paymentAccountResponse.setUserId(paymentAccount.getUserId());
        paymentAccountResponse.setBalance(paymentAccount.getBalance());
        paymentAccountResponse.setActive(paymentAccount.isActive());
        List<Long> transactionIds = paymentAccount.getTransactions().stream().map(Transaction::getId).toList();
        paymentAccountResponse.setTransactionIds(transactionIds);
        paymentAccountResponse.setCreatedAt(paymentAccount.getCreatedAt());
        paymentAccountResponse.setUpdatedAt(paymentAccount.getUpdatedAt());
        return paymentAccountResponse;
    }

    public List<PaymentAccountResponse> toResponses(List<PaymentAccount> paymentAccounts) {
        return paymentAccounts.stream().map(this::toResponse).toList();
    }
}
