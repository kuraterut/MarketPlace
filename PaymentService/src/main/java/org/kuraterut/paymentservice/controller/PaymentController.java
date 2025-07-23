package org.kuraterut.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.usecases.paymentaccount.CreatePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.DeletePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.GetPaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.UpdatePaymentAccountUseCase;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    //TODO Пагинация
    //TODO Прописать Postman
    //TODO Добавить тестовые данные в Ликви

    private final CreatePaymentAccountUseCase createPaymentAccountUseCase;
    private final GetPaymentAccountUseCase getPaymentAccountUseCase;
    private final DeletePaymentAccountUseCase deletePaymentAccountUseCase;
    private final UpdatePaymentAccountUseCase updatePaymentAccountUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    public PaymentAccountResponse createBankAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return createPaymentAccountUseCase.createPaymentAccount(userId);
    }
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<PaymentAccountResponse> adminGetAllPaymentAccounts() {
        return getPaymentAccountUseCase.getAllPaymentAccounts();
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PaymentAccountResponse adminGetPaymentAccountById(@PathVariable("id") Long id) {
        return getPaymentAccountUseCase.getPaymentAccountById(id);
    }

    @GetMapping("/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PaymentAccountResponse adminGetPaymentAccountByUserId(@PathVariable("id") Long userId) {
        return getPaymentAccountUseCase.getPaymentAccountByUserId(userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    public PaymentAccountResponse getPaymentAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getPaymentAccountUseCase.getPaymentAccountByUserId(userId);
    }

    @GetMapping("/filter/active")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<PaymentAccountResponse> getPaymentAccountsFilterByActive(@RequestParam("isActive") Boolean isActive) {
        return getPaymentAccountUseCase.getPaymentAccountsByIsActive(isActive);
    }

    @GetMapping("/filter/balance")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<PaymentAccountResponse> getPaymentAccountsFilterByBalanceBetween(@RequestParam("min") BigDecimal min,
                                                                        @RequestParam("max") BigDecimal max) {
        return getPaymentAccountUseCase.getPaymentAccountsByBalanceBetween(min, max);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    public void deletePaymentAccountByUserId(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        deletePaymentAccountUseCase.deletePaymentAccountByUserId(userId);
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void adminDeletePaymentAccount(@PathVariable("id") Long id) {
        deletePaymentAccountUseCase.deletePaymentAccountById(id);
    }

    @DeleteMapping("/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void adminDeletePaymentAccountByUserId(@PathVariable("id") Long userId) {
        deletePaymentAccountUseCase.deletePaymentAccountByUserId(userId);
    }


    @PutMapping("/deposit")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    public PaymentAccountResponse depositPaymentAccountByUserId(@RequestParam("amount") BigDecimal amount,
                                                     @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.depositPaymentAccountByUserId(authPrincipal.getUserId(), amount);
    }

    @PutMapping("/withdraw")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    public PaymentAccountResponse withdrawPaymentAccountByUserId(@RequestParam("amount") BigDecimal amount,
                                                      @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.withdrawPaymentAccountByUserId(authPrincipal.getUserId(), amount);
    }




    @PutMapping("/activate")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    public PaymentAccountResponse activatePaymentAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.activatePaymentAccountByUserId(authPrincipal.getUserId());
    }

    @PutMapping("/activate/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PaymentAccountResponse adminActivatePaymentAccount(@PathVariable("id") Long id) {
        return updatePaymentAccountUseCase.activatePaymentAccountById(id);
    }

    @PutMapping("/activate/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PaymentAccountResponse adminActivatePaymentAccountByUserId(@PathVariable("id") Long userId) {
        return updatePaymentAccountUseCase.activatePaymentAccountByUserId(userId);
    }

    @PutMapping("/deactivate")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    public PaymentAccountResponse deactivatePaymentAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.deactivatePaymentAccountByUserId(authPrincipal.getUserId());
    }

    @PutMapping("/deactivate/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PaymentAccountResponse adminDeactivatePaymentAccount(@PathVariable("id") Long id) {
        return updatePaymentAccountUseCase.deactivatePaymentAccountById(id);
    }

    @PutMapping("/deactivate/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PaymentAccountResponse adminDeactivatePaymentAccountByUserId(@PathVariable("id") Long userId) {
        return updatePaymentAccountUseCase.deactivatePaymentAccountByUserId(userId);
    }

}
