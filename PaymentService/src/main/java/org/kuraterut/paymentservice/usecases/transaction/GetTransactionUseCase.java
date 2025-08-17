package org.kuraterut.paymentservice.usecases.transaction;

import org.kuraterut.paymentservice.dto.response.TransactionListResponse;
import org.kuraterut.paymentservice.dto.response.TransactionResponse;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface GetTransactionUseCase {
    TransactionListResponse getAllTransactionsAndUserId(Long userId, Pageable pageable);
    TransactionResponse getTransactionByIdAndUserId(Long id, Long userId);
    TransactionListResponse getTransactionsByAmountBetweenAndUserId(BigDecimal min, BigDecimal max, Long userId, Pageable pageable);
    TransactionListResponse getTransactionsByTransactionTypeAndUserId(TransactionType type, Long userId, Pageable pageable);
    TransactionListResponse getTransactionsByTransactionStatusAndUserId(TransactionStatus status, Long userId, Pageable pageable);
    TransactionListResponse getTransactionsByOrderIdAndUserId(Long orderId, Long userId, Pageable pageable);

    TransactionResponse getTransactionById(Long id);
    TransactionListResponse getAllTransactions(Pageable pageable);
    TransactionListResponse getTransactionsByPaymentAccountId(Long paymentAccountId, Pageable pageable);
    TransactionListResponse getTransactionsByAmountBetween(BigDecimal min, BigDecimal max, Pageable pageable);
    TransactionListResponse getTransactionsByTransactionType(TransactionType type, Pageable pageable);
    TransactionListResponse getTransactionsByTransactionStatus(TransactionStatus status, Pageable pageable);
    TransactionListResponse getTransactionsByOrderId(Long orderId, Pageable pageable);
}
