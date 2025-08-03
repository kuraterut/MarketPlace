package org.kuraterut.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.kuraterut.paymentservice.dto.response.PaymentAccountListResponse;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.usecases.paymentaccount.CreatePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.DeletePaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.GetPaymentAccountUseCase;
import org.kuraterut.paymentservice.usecases.paymentaccount.UpdatePaymentAccountUseCase;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Payment controller", description = "Controller for payment account manipulation")
public class PaymentController {
    //TODO Добавить тестовые данные в Ликви

    private final CreatePaymentAccountUseCase createPaymentAccountUseCase;
    private final GetPaymentAccountUseCase getPaymentAccountUseCase;
    private final DeletePaymentAccountUseCase deletePaymentAccountUseCase;
    private final UpdatePaymentAccountUseCase updatePaymentAccountUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    @Operation(summary = "Create Payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Payment Account is already exists"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse createPaymentAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return createPaymentAccountUseCase.createPaymentAccount(userId);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting all Payment accounts (Pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment accounts found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountListResponse adminGetAllPaymentAccounts(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getPaymentAccountUseCase.getAllPaymentAccounts(pageable);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting payment account by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse adminGetPaymentAccountById(
            @Parameter(description = "Account ID") @PathVariable("id") Long id) {
        return getPaymentAccountUseCase.getPaymentAccountById(id);
    }

    @GetMapping("/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting payment account by User ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse adminGetPaymentAccountByUserId(
            @Parameter(description = "User ID") @PathVariable("id") Long userId) {
        return getPaymentAccountUseCase.getPaymentAccountByUserId(userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    @Operation(summary = "Getting payment account by User ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse getPaymentAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        return getPaymentAccountUseCase.getPaymentAccountByUserId(userId);
    }

    @GetMapping("/filter/active")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting payment accounts filter by active flag")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Accounts found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountListResponse getPaymentAccountsFilterByActive(
            @Parameter(description = "Is active flag") @RequestParam("isActive") Boolean isActive,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getPaymentAccountUseCase.getPaymentAccountsByIsActive(isActive, pageable);
    }

    @GetMapping("/filter/balance")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting payment accounts filter by balance between")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Accounts found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountListResponse getPaymentAccountsFilterByBalanceBetween(
            @Parameter(description = "Min value of balance") @RequestParam("min") BigDecimal min,
            @Parameter(description = "Max value of balance") @RequestParam("max") BigDecimal max,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getPaymentAccountUseCase.getPaymentAccountsByBalanceBetween(min, max, pageable);
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    @Operation(summary = "Delete payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "409", description = "Payment Account has non zero balance"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public void deletePaymentAccountByUserId(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Long userId = authPrincipal.getUserId();
        deletePaymentAccountUseCase.deletePaymentAccountByUserId(userId);
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "409", description = "Payment Account has non zero balance"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public void adminDeletePaymentAccount(
            @Parameter(description = "Account ID") @PathVariable("id") Long id) {
        deletePaymentAccountUseCase.deletePaymentAccountById(id);
    }

    @DeleteMapping("/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "409", description = "Payment Account has non zero balance"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public void adminDeletePaymentAccountByUserId(
            @Parameter(description = "User ID") @PathVariable("id") Long userId) {
        deletePaymentAccountUseCase.deletePaymentAccountByUserId(userId);
    }

    //TODO Сделать User ID - первичным ключом


    @PutMapping("/deposit")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    @Operation(summary = "Deposit payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account deposited"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "304", description = "Payment Account Not Modified"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse depositPaymentAccountByUserId(
            @Parameter(description = "Amount to deposit") @RequestParam("amount") BigDecimal amount,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.depositPaymentAccountByUserId(authPrincipal.getUserId(), amount);
    }

    @PutMapping("/withdraw")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    @Operation(summary = "Withdraw payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account withdraw successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "304", description = "Payment Account Not Modified"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse withdrawPaymentAccountByUserId(
            @Parameter(description = "Amount to withdraw") @RequestParam("amount") BigDecimal amount,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.withdrawPaymentAccountByUserId(authPrincipal.getUserId(), amount);
    }




    @PutMapping("/activate")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    @Operation(summary = "Activate payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account activated "),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "304", description = "Payment Account Not Modified"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse activatePaymentAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.activatePaymentAccountByUserId(authPrincipal.getUserId());
    }

    @PutMapping("/activate/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Activate payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account activated "),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "304", description = "Payment Account Not Modified"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse adminActivatePaymentAccount(@PathVariable("id") Long id) {
        return updatePaymentAccountUseCase.activatePaymentAccountById(id);
    }

    @PutMapping("/activate/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Activate payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account activated "),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "304", description = "Payment Account Not Modified"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse adminActivatePaymentAccountByUserId(@PathVariable("id") Long userId) {
        return updatePaymentAccountUseCase.activatePaymentAccountByUserId(userId);
    }

    @PutMapping("/deactivate")
    @PreAuthorize("hasAuthority('SELLER') or hasAuthority('CUSTOMER')")
    @Operation(summary = "Deactivate payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account deactivated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "304", description = "Payment Account Not Modified"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse deactivatePaymentAccount(@AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return updatePaymentAccountUseCase.deactivatePaymentAccountByUserId(authPrincipal.getUserId());
    }

    @PutMapping("/deactivate/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Deactivate payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account deactivated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "304", description = "Payment Account Not Modified"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse adminDeactivatePaymentAccount(@PathVariable("id") Long id) {
        return updatePaymentAccountUseCase.deactivatePaymentAccountById(id);
    }

    @PutMapping("/deactivate/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Deactivate payment account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Account deactivated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "304", description = "Payment Account Not Modified"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public PaymentAccountResponse adminDeactivatePaymentAccountByUserId(@PathVariable("id") Long userId) {
        return updatePaymentAccountUseCase.deactivatePaymentAccountByUserId(userId);
    }

}
