package org.kuraterut.bankaccount.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kuraterut.bankaccount.model.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBankAccountRequest {
    @NotBlank
    private Currency currency;
}
