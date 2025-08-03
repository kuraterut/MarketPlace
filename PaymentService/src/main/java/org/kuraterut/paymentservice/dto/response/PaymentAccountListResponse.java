package org.kuraterut.paymentservice.dto.response;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAccountListResponse {
    List<PaymentAccountResponse> paymentAccounts;
}
