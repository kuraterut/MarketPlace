package org.kuraterut.bankaccount.mapper;

import org.kuraterut.bankaccount.dto.request.CreateBankAccountRequest;
import org.kuraterut.bankaccount.dto.response.BankAccountResponse;
import org.kuraterut.bankaccount.model.BankAccount;
import org.kuraterut.bankaccount.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class BankAccountMapper {
    public BankAccount toEntity(CreateBankAccountRequest request, Long userId) {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setUserId(userId);
        bankAccount.setActive(true);
        bankAccount.setCurrency(request.getCurrency());
        bankAccount.setBalance(BigDecimal.ZERO);
        return bankAccount;
    }

    public BankAccountResponse toResponse(BankAccount bankAccount) {
        BankAccountResponse bankAccountResponse = new BankAccountResponse();
        bankAccountResponse.setId(bankAccount.getId());
        bankAccountResponse.setUserId(bankAccount.getUserId());
        bankAccountResponse.setBalance(bankAccount.getBalance());
        bankAccountResponse.setCurrency(bankAccount.getCurrency());
        bankAccountResponse.setActive(bankAccount.isActive());
        List<Long> transactionIds = bankAccount.getTransactions().stream().map(Transaction::getId).toList();
        bankAccountResponse.setTransactionIds(transactionIds);
        bankAccountResponse.setCreatedAt(bankAccount.getCreatedAt());
        bankAccountResponse.setUpdatedAt(bankAccount.getUpdatedAt());
        return bankAccountResponse;
    }

    public List<BankAccountResponse> toResponses(List<BankAccount> bankAccounts) {
        return bankAccounts.stream().map(this::toResponse).toList();
    }
}
