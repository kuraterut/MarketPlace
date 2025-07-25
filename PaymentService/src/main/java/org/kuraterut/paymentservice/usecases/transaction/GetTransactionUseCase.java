package org.kuraterut.paymentservice.usecases.transaction;

import org.kuraterut.paymentservice.dto.response.TransactionResponse;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;

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
