package org.kuraterut.paymentservice.mapper;

import org.kuraterut.paymentservice.dto.request.CreateTransactionRequest;
import org.kuraterut.paymentservice.dto.response.TransactionListResponse;
import org.kuraterut.paymentservice.dto.response.TransactionResponse;
import org.kuraterut.paymentservice.model.entity.Transaction;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setType(request.getTransactionType());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setOrderId(request.getOrderId());
        return transaction;
    }

    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setId(transaction.getId());
        transactionResponse.setPaymentAccountId(transaction.getAccount().getId());
        transaction.setAmount(transaction.getAmount());
        transactionResponse.setType(transaction.getType());
        transactionResponse.setDescription(transaction.getDescription());
        transactionResponse.setStatus(transaction.getStatus());
        transactionResponse.setOrderId(transaction.getOrderId());
        transactionResponse.setCreatedAt(transaction.getCreatedAt().toString());
        transactionResponse.setUpdatedAt(transaction.getUpdatedAt().toString());
        return transactionResponse;
    }

    public TransactionListResponse toResponses(Page<Transaction> transactions) {
        return new TransactionListResponse(
                transactions.map(this::toResponse).stream().toList()
        );
    }
}
