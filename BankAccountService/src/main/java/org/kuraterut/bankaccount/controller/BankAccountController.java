package org.kuraterut.bankaccount.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.bankaccount.dto.request.CreateBankAccountRequest;
import org.kuraterut.bankaccount.dto.response.BankAccountResponse;
import org.kuraterut.bankaccount.model.Currency;
import org.kuraterut.bankaccount.usecases.bankaccount.CreateBankAccountUseCase;
import org.kuraterut.bankaccount.usecases.bankaccount.DeleteBankAccountUseCase;
import org.kuraterut.bankaccount.usecases.bankaccount.GetBankAccountUseCase;
import org.kuraterut.bankaccount.usecases.bankaccount.UpdateBankAccountUseCase;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {
    //TODO Добавить проверку прав доступа
    //TODO Валидация
    //TODO Пагинация
    //TODO Прописать Postman

    private final CreateBankAccountUseCase createBankAccountUseCase;
    private final GetBankAccountUseCase getBankAccountUseCase;
    private final DeleteBankAccountUseCase deleteBankAccountUseCase;
    private final UpdateBankAccountUseCase updateBankAccountUseCase;

    @PostMapping
    public BankAccountResponse createBankAccount(@RequestBody @Valid CreateBankAccountRequest request,
                                                                 @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return createBankAccountUseCase.createBankAccount(request, userId);
    }
    @GetMapping
    public List<BankAccountResponse> getBankAccounts(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getBankAccountUseCase.getBankAccountsByUserId(userId);
    }

    @GetMapping("/{id}")
    public BankAccountResponse getBankAccount(@PathVariable("id") Long id, @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getBankAccountUseCase.getBankAccountById(id, userId);
    }

    @GetMapping("/filter/currency")
    public List<BankAccountResponse> getBankAccountsByCurrency(@RequestParam("currency") Currency currency,
                                                               @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getBankAccountUseCase.getBankAccountsByCurrency(currency, userId);
    }

    @GetMapping("/filter/active")
    public List<BankAccountResponse> getBankAccountsByActive(@RequestParam("isActive") Boolean isActive,
                                                             @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getBankAccountUseCase.getBankAccountsByIsActive(isActive, userId);
    }

    @GetMapping("/filter/balance")
    public List<BankAccountResponse> getBankAccountsByBalanceBetween(@RequestParam("min") BigDecimal min,
                                                                     @RequestParam("max") BigDecimal max,
                                                                     @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getBankAccountUseCase.getBankAccountsByBalanceBetween(min, max, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteBankAccount(@PathVariable("id") Long id, @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        deleteBankAccountUseCase.deleteBankAccountById(id, userId);
    }

    @DeleteMapping("/user")
    public void deleteBankAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        deleteBankAccountUseCase.deleteBankAccountsByUserId(userId);
    }

    @PutMapping("/deposit/{id}")
    public BankAccountResponse depositBankAccount(@PathVariable("id") Long id,
                                                  @RequestParam("amount") BigDecimal amount,
                                                  @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updateBankAccountUseCase.depositBankAccount(id, amount, authPrincipal.getUserId());
    }

    @PutMapping("/withdraw/{id}")
    public BankAccountResponse withdrawBankAccount(@PathVariable("id") Long id,
                                                  @RequestParam("amount") BigDecimal amount,
                                                  @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updateBankAccountUseCase.withdrawBankAccount(id, amount, authPrincipal.getUserId());
    }

    @PutMapping("/activate/{id}")
    public BankAccountResponse activateBankAccount(@PathVariable("id") Long id,
                                                   @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updateBankAccountUseCase.activateBankAccount(id, authPrincipal.getUserId());
    }

    @PutMapping("/deactivate/{id}")
    public BankAccountResponse deactivateBankAccount(@PathVariable("id") Long id,
                                                   @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updateBankAccountUseCase.deactivateBankAccount(id, authPrincipal.getUserId());
    }

}
