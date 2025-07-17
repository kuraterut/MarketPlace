package org.kuraterut.bankaccount.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.bankaccount.model.TransactionStatus;
import org.kuraterut.bankaccount.model.TransactionType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTransactionRequest {
    @NotNull
    private Long accountId;

    @Positive @NotNull
    private BigDecimal amount;

    @NotNull
    private TransactionType transactionType;

    private String description;

    @NotNull
    private TransactionStatus status;

    private Long orderId;
}
