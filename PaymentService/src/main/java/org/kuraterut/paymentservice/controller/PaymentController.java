package org.kuraterut.paymentservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.paymentservice.dto.request.CreatePaymentAccountRequest;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.model.Currency;
import org.kuraterut.paymentservice.usecases.bankaccount.CreatePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.bankaccount.DeletePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.bankaccount.GetPaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.bankaccount.UpdatePaymentAccountUseCase;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    //TODO Добавить проверку прав доступа
    //TODO Валидация
    //TODO Пагинация
    //TODO Прописать Postman

    private final CreatePaymentAccountUseCase createPaymentAccountUseCase;
    private final GetPaymentAccountUseCase getPaymentAccountUseCase;
    private final DeletePaymentAccountUseCase deletePaymentAccountUseCase;
    private final UpdatePaymentAccountUseCase updatePaymentAccountUseCase;

    @PostMapping
    public PaymentAccountResponse createBankAccount(@RequestBody @Valid CreatePaymentAccountRequest request,
                                                    @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return createPaymentAccountUseCase.createBankAccount(request, userId);
    }
    @GetMapping
    public List<PaymentAccountResponse> getBankAccounts(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getPaymentAccountUseCase.getBankAccountsByUserId(userId);
    }

    @GetMapping("/{id}")
    public PaymentAccountResponse getBankAccount(@PathVariable("id") Long id, @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getPaymentAccountUseCase.getBankAccountById(id, userId);
    }

    @GetMapping("/filter/currency")
    public List<PaymentAccountResponse> getBankAccountsByCurrency(@RequestParam("currency") Currency currency,
                                                                  @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getPaymentAccountUseCase.getBankAccountsByCurrency(currency, userId);
    }

    @GetMapping("/filter/active")
    public List<PaymentAccountResponse> getBankAccountsByActive(@RequestParam("isActive") Boolean isActive,
                                                                @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getPaymentAccountUseCase.getBankAccountsByIsActive(isActive, userId);
    }

    @GetMapping("/filter/balance")
    public List<PaymentAccountResponse> getBankAccountsByBalanceBetween(@RequestParam("min") BigDecimal min,
                                                                        @RequestParam("max") BigDecimal max,
                                                                        @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getPaymentAccountUseCase.getBankAccountsByBalanceBetween(min, max, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteBankAccount(@PathVariable("id") Long id, @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        deletePaymentAccountUseCase.deleteBankAccountById(id, userId);
    }

    @DeleteMapping("/user")
    public void deleteBankAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        deletePaymentAccountUseCase.deleteBankAccountsByUserId(userId);
    }

    @PutMapping("/deposit/{id}")
    public PaymentAccountResponse depositBankAccount(@PathVariable("id") Long id,
                                                     @RequestParam("amount") BigDecimal amount,
                                                     @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.depositBankAccount(id, amount, authPrincipal.getUserId());
    }

    @PutMapping("/withdraw/{id}")
    public PaymentAccountResponse withdrawBankAccount(@PathVariable("id") Long id,
                                                      @RequestParam("amount") BigDecimal amount,
                                                      @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.withdrawBankAccount(id, amount, authPrincipal.getUserId());
    }

    @PutMapping("/activate/{id}")
    public PaymentAccountResponse activateBankAccount(@PathVariable("id") Long id,
                                                      @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.activateBankAccount(id, authPrincipal.getUserId());
    }

    @PutMapping("/deactivate/{id}")
    public PaymentAccountResponse deactivateBankAccount(@PathVariable("id") Long id,
                                                        @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.deactivateBankAccount(id, authPrincipal.getUserId());
    }

}
