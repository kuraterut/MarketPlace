package org.kuraterut.paymentservice.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.paymentservice.model.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePaymentAccountRequest {
    @NotBlank
    private Currency currency;
}
