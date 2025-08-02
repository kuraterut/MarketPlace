package org.kuraterut.paymentservice.mapper;

import org.kuraterut.paymentservice.dto.response.PaymentAccountListResponse;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.model.entity.PaymentAccount;
import org.kuraterut.paymentservice.model.entity.Transaction;
import org.springframework.data.domain.Page;
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
        paymentAccountResponse.setCreatedAt(paymentAccount.getCreatedAt().toString());
        paymentAccountResponse.setUpdatedAt(paymentAccount.getUpdatedAt().toString());
        return paymentAccountResponse;
    }

    public PaymentAccountListResponse toResponses(Page<PaymentAccount> paymentAccounts) {
        return new PaymentAccountListResponse(paymentAccounts.map(this::toResponse).stream().toList());
    }
}
