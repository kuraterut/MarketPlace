package org.kuraterut.paymentservice.usecases.transaction;

import org.kuraterut.paymentservice.dto.request.CreateTransactionRequest;
import org.kuraterut.paymentservice.dto.response.TransactionResponse;

public interface CreateTransactionUseCase {
    TransactionResponse createTransaction(CreateTransactionRequest request, Long userId);

}
