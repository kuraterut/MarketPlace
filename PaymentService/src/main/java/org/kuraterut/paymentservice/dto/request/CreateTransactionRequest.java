package org.kuraterut.paymentservice.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;

import java.math.BigDecimal;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request of transaction creating")
public class CreateTransactionRequest {
    @Positive(message = "Transaction amount must be positive")
    @NotNull(message = "Transaction amount must be not null")
    @Schema(description = "Transaction amount", example = "100.0", requiredMode = REQUIRED)
    private BigDecimal amount;

    @NotNull(message = "Transaction type must be not null")
    @Schema(description = "Transaction type", example = "DEPOSIT", requiredMode = REQUIRED)
    private TransactionType transactionType;

    @Schema(description = "Transaction description", example = "Deposit payment account", requiredMode = NOT_REQUIRED)
    private String description;

//    @NotNull(message = "Order ID must be not null")
    @Schema(description = "Transaction order ID", example = "1", requiredMode = NOT_REQUIRED)
    private Long orderId;
}
