package org.kuraterut.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderListResponse;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.kuraterut.orderservice.usecases.CreateOrderUseCase;
import org.kuraterut.orderservice.usecases.GetOrderUseCase;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Token")
@Tag(name = "Order controller", description = "Controller for order manipulations")
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Creating new order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created successfully, return order info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderResponse createOrder(
            @Parameter(description = "Create order request")
            @RequestBody @Valid CreateOrderRequest request,
            @AuthenticationPrincipal AuthPrincipal authPrincipal) throws JsonProcessingException {
        return createOrderUseCase.createOrder(request, authPrincipal.getUserId());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting order info by Order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found successfully, return order info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderResponse getOrderById(
            @Parameter(description = "Order ID") @PathVariable("id") Long id) {
        return getOrderUseCase.getOrderById(id);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting all orders (Pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found successfully, return orders info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderListResponse getAllOrders(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction)  {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrders(pageable);
    }

    @GetMapping("/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting all user orders (Pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found successfully, return orders info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderListResponse getAllOrdersByUserId(
            @Parameter(description = "User ID") @PathVariable("id") Long userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByUserId(userId, pageable);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Getting all user orders (Pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found successfully, return orders info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderListResponse getAllOrdersByUserId(
            @AuthenticationPrincipal AuthPrincipal authPrincipal,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByUserId(authPrincipal.getUserId(), pageable);
    }

    @GetMapping("/admin/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting all orders by status (Pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found successfully, return orders info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderListResponse getAllOrdersByOrderStatus(
            @Parameter(description = "Order Status") @RequestParam("status") OrderStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByOrderStatus(status, pageable);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Getting all user orders by status (Pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found successfully, return orders info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderListResponse getAllOrdersByOrderStatusAndUserId(
            @Parameter(description = "Order status") @RequestParam("status") OrderStatus status,
            @AuthenticationPrincipal AuthPrincipal authPrincipal,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByOrderStatus(status, authPrincipal.getUserId(), pageable);
    }

    //TODO Прописать Postman для Created At
    @GetMapping("/admin/createdAt")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Getting all orders filter by creating timestamp (Pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found successfully, return orders info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderListResponse getAllOrdersByCreatedAtAfter(
            @Parameter(description = "Created At timestamp") @RequestParam("createdAt") OffsetDateTime createdAt,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByCreatedAtAfter(createdAt, pageable);
    }

    @GetMapping("/createdAt")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Getting of all user orders filter by creating timestamp (Pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found successfully, return orders info"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public OrderListResponse getAllOrdersByCreatedAtAfterAndUserId(
            @Parameter(description = "Created at timestamp") @RequestParam("createdAt") OffsetDateTime createdAt,
            @AuthenticationPrincipal AuthPrincipal authPrincipal,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Page sorting attribute") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Page sorting direction") @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByCreatedAtAfter(createdAt, authPrincipal.getUserId(), pageable);
    }
}
