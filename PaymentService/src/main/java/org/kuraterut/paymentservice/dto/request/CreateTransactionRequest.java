package org.kuraterut.paymentservice.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTransactionRequest {
    @NotNull(message = "Account ID must be not null")
    private Long accountId;

    @Positive(message = "Transaction amount must be positive")
    @NotNull(message = "Transaction amount must be not null")
    private BigDecimal amount;

    @NotNull(message = "Transaction type must be not null")
    private TransactionType transactionType;

    private String description;

    @NotNull(message = "Transaction status must be not null")
    private TransactionStatus status;

    @NotNull(message = "Order ID must be not null")
    private Long orderId;
}
