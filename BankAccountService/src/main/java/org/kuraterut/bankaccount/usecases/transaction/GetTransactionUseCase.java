package org.kuraterut.bankaccount.usecases.transaction;

import org.kuraterut.bankaccount.dto.response.TransactionResponse;
import org.kuraterut.bankaccount.model.TransactionStatus;
import org.kuraterut.bankaccount.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;

public interface GetTransactionUseCase {
    List<TransactionResponse> getAllTransactions();
    TransactionResponse getTransactionById(Long id);
    List<TransactionResponse> getTransactionsByBankAccountId(Long bankAccountId);
    List<TransactionResponse> getTransactionsByAmountBetween(BigDecimal min, BigDecimal max);
    List<TransactionResponse> getTransactionsByTransactionType(TransactionType type);
    List<TransactionResponse> getTransactionsByTransactionStatus(TransactionStatus status);
    List<TransactionResponse> getTransactionsByOrderId(Long orderId);
}
