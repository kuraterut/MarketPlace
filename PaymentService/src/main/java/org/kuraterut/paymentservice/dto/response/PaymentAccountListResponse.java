package org.kuraterut.paymentservice.dto.response;

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
