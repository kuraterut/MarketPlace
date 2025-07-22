package org.kuraterut.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.paymentservice.model.TransactionStatus;
import org.kuraterut.paymentservice.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private Long bankAccountId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private TransactionStatus status;
    private Long orderId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
