package org.kuraterut.paymentservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response with transaction info")
public class TransactionResponse {
    @Schema(description = "Transaction ID", example = "1")
    private Long id;
    @Schema(description = "Transaction Payment Account", example = "1")
    private Long paymentAccountId;
    @Schema(description = "Transaction amount", example = "100.0")
    private BigDecimal amount;
    @Schema(description = "Transaction type", example = "DEPOSIT")
    private TransactionType type;
    @Schema(description = "Transaction description", example = "Deposit Payment Account")
    private String description;
    @Schema(description = "Transaction status", example = "COMPLETED")
    private TransactionStatus status;
    @Schema(description = "Transaction Order ID", example = "1")
    private Long orderId;
    @Schema(description = "Creating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String createdAt;
    @Schema(description = "Updating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String updatedAt;
}
