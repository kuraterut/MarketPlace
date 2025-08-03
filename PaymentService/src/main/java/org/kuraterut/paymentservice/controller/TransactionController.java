package org.kuraterut.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.kuraterut.paymentservice.dto.request.CreateTransactionRequest;
import org.kuraterut.paymentservice.dto.response.TransactionListResponse;
import org.kuraterut.paymentservice.dto.response.TransactionResponse;
import org.kuraterut.paymentservice.model.entity.Transaction;
import org.kuraterut.paymentservice.model.utils.TransactionStatus;
import org.kuraterut.paymentservice.model.utils.TransactionType;
import org.kuraterut.paymentservice.repository.TransactionRepository;
import org.kuraterut.paymentservice.usecases.transaction.CreateTransactionUseCase;
import org.kuraterut.paymentservice.usecases.transaction.GetTransactionUseCase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments/transaction")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Transaction controller", description = "Controller for transactions manipulation")
public class TransactionController {
    private final GetTransactionUseCase getTransactionUseCase;
    private final CreateTransactionUseCase createTransactionUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    @Operation(summary = "Create Transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionResponse createTransaction(
            @Parameter(description = "Create Transaction Request") @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return createTransactionUseCase.createTransaction(request, authPrincipal.getUserId());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    @Operation(summary = "Getting All User Transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getAllTransactions(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Long userId = authPrincipal.getUserId();
        return getTransactionUseCase.getAllTransactions(userId, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    @Operation(summary = "Getting User Transaction By ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "404", description = "Transaction Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionResponse getTransaction(
            @Parameter(description = "Transaction ID") @PathVariable("id") Long id,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) {
        return getTransactionUseCase.getTransactionById(id, authPrincipal.getUserId());
    }

    @GetMapping("/filter/amount")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    @Operation(summary = "Getting All User Transactions filtered By Amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByAmount(
            @Parameter(description = "Minimal Amount") @RequestParam("min") BigDecimal min,
            @Parameter(description = "Maximum Amount") @RequestParam("max") BigDecimal max,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction,
            @AuthenticationPrincipal AuthPrincipal authPrincipal){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Long userId = authPrincipal.getUserId();
        return getTransactionUseCase.getTransactionsByAmountBetween(min, max, userId, pageable);
    }

    @GetMapping("/filter/type")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    @Operation(summary = "Getting All User Transactions filtered By Type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByType(
            @Parameter(description = "Transaction Type") @RequestParam("type") TransactionType type,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction,
            @AuthenticationPrincipal AuthPrincipal authPrincipal){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Long userId = authPrincipal.getUserId();
        return getTransactionUseCase.getTransactionsByTransactionType(type, userId, pageable);
    }

    @GetMapping("/filter/status")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    @Operation(summary = "Getting All User Transactions filtered By Status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByStatus(
            @Parameter(description = "Transaction Status") @RequestParam("status") TransactionStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction,
            @AuthenticationPrincipal AuthPrincipal authPrincipal){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Long userId = authPrincipal.getUserId();
        return getTransactionUseCase.getTransactionsByTransactionStatus(status, userId, pageable);
    }

    @GetMapping("/filter/order")
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('SELLER')")
    @Operation(summary = "Getting All User Transactions filtered By Order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Payment Account Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByOrder(
            @Parameter(description = "Order ID") @RequestParam("orderId") Long orderId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction,
            @AuthenticationPrincipal AuthPrincipal authPrincipal){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Long userId = authPrincipal.getUserId();
        return getTransactionUseCase.getTransactionsByOrderId(orderId, userId, pageable);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting Transaction By ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Transaction Not Found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionResponse getTransaction(
            @Parameter(description = "Transaction ID") @PathVariable("id") Long id){
        return getTransactionUseCase.getTransactionById(id);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting All Transactions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getAllTransactions(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getTransactionUseCase.getAllTransactions(pageable);
    }

    @GetMapping("/admin/filter/account")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting Transaction Filtered By Account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByAccountId(
            @Parameter(description = "Account ID") @RequestParam("accountId") Long accountId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getTransactionUseCase.getTransactionsByPaymentAccountId(accountId, pageable);
    }

    @GetMapping("/admin/filter/amount")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting Transaction Filtered By Amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByAmount(
            @Parameter(description = "Minimal Amount") @RequestParam("min") BigDecimal min,
            @Parameter(description = "Maximum Amount") @RequestParam("max") BigDecimal max,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getTransactionUseCase.getTransactionsByAmountBetween(min, max, pageable);
    }

    @GetMapping("/admin/filter/type")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting Transaction Filtered By Transaction Type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByType(
            @Parameter(description = "Transaction Type") @RequestParam("type") TransactionType type,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getTransactionUseCase.getTransactionsByTransactionType(type, pageable);
    }

    @GetMapping("/admin/filter/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting Transaction Filtered By Transaction Status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByStatus(
            @Parameter(description = "Transaction Status") @RequestParam("status") TransactionStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getTransactionUseCase.getTransactionsByTransactionStatus(status, pageable);
    }

    @GetMapping("/admin/filter/order")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting Transaction Filtered By Order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public TransactionListResponse getTransactionsFilteredByOrder(
            @Parameter(description = "Order ID") @RequestParam("orderId") Long orderId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getTransactionUseCase.getTransactionsByOrderId(orderId, pageable);
    }
}
