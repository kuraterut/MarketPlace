package org.kuraterut.paymentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentAccountResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private boolean isActive;
    private List<Long> transactionIds;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
