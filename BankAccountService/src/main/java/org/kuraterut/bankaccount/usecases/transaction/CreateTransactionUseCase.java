package org.kuraterut.bankaccount.usecases.transaction;

import org.kuraterut.bankaccount.dto.request.CreateTransactionRequest;
import org.kuraterut.bankaccount.dto.response.TransactionResponse;
import org.kuraterut.bankaccount.model.Transaction;

public interface CreateTransactionUseCase {
    TransactionResponse createTransaction(CreateTransactionRequest request);
}
