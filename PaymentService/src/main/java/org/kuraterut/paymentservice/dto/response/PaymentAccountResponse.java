package org.kuraterut.paymentservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response with payment account info")
public class PaymentAccountResponse {
    @Schema(description = "Payment Account ID", example = "1")
    private Long id;
    @Schema(description = "Payment Account User ID", example = "1")
    private Long userId;
    @Schema(description = "Payment Account Balance", example = "100.0")
    private BigDecimal balance;
    @Schema(description = "Is Payment Account Active flag", example = "true")
    private boolean isActive;
    @Schema(description = "Creating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String createdAt;
    @Schema(description = "Updating Timestamp", example = "2025-12-03T10:15:30+01:00")
    private String updatedAt;

}
