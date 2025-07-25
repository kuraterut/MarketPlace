package org.kuraterut.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.utils.OrderStatus;
import org.kuraterut.orderservice.usecases.CreateOrderUseCase;
import org.kuraterut.orderservice.usecases.GetOrderUseCase;
import org.springframework.data.domain.Page;
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
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public OrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request,
                                     @AuthenticationPrincipal AuthPrincipal authPrincipal) throws JsonProcessingException {
        return createOrderUseCase.createOrder(request, authPrincipal.getUserId());
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public OrderResponse getOrderById(@PathVariable("id") Long id) {
        return getOrderUseCase.getOrderById(id);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<OrderResponse> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(defaultValue = "id") String sortBy,
                                            @RequestParam(defaultValue = "asc") String direction)  {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrders(pageable);
    }

    @GetMapping("/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<OrderResponse> getAllOrdersByUserId(@PathVariable("id") Long userId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "id") String sortBy,
                                                    @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByUserId(userId, pageable);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public Page<OrderResponse> getAllOrdersByUserId(@AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(defaultValue = "id") String sortBy,
                                                    @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByUserId(authPrincipal.getUserId(), pageable);
    }

    @GetMapping("/admin/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<OrderResponse> getAllOrdersByOrderStatus(@RequestParam("status") OrderStatus status,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(defaultValue = "id") String sortBy,
                                                         @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByOrderStatus(status, pageable);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public Page<OrderResponse> getAllOrdersByOrderStatusAndUserId(@RequestParam("status") OrderStatus status,
                                                                  @AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "id") String sortBy,
                                                                  @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByOrderStatus(status, authPrincipal.getUserId(), pageable);
    }
    @GetMapping("/admin/createdAt")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<OrderResponse> getAllOrdersByCreatedAtAfter(@RequestParam("createdAt") OffsetDateTime createdAt,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(defaultValue = "id") String sortBy,
                                                            @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByCreatedAtAfter(createdAt, pageable);
    }

    @GetMapping("/createdAt")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public Page<OrderResponse> getAllOrdersByCreatedAtAfterAndUserId(@RequestParam("createdAt") OffsetDateTime createdAt,
                                                                     @AuthenticationPrincipal AuthPrincipal authPrincipal,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestParam(defaultValue = "id") String sortBy,
                                                                     @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return getOrderUseCase.getAllOrdersByCreatedAtAfter(createdAt, authPrincipal.getUserId(), pageable);
    }
}
