package org.kuraterut.bankaccount.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.bankaccount.model.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankAccountResponse {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private Currency currency;
    private boolean isActive;
    private List<Long> transactionIds;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
