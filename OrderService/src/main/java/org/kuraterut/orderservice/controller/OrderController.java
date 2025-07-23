package org.kuraterut.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.kuraterut.orderservice.dto.request.CreateOrderRequest;
import org.kuraterut.orderservice.dto.response.OrderResponse;
import org.kuraterut.orderservice.model.OrderStatus;
import org.kuraterut.orderservice.service.OrderService;
import org.kuraterut.orderservice.usecases.CreateOrderUseCase;
import org.kuraterut.orderservice.usecases.GetOrderUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    //TODO Пагинация
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
    public OrderResponse getOrderById(@PathVariable("id") Long id) throws JsonProcessingException {
        return getOrderUseCase.getOrderById(id);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<OrderResponse> getAllOrders() throws JsonProcessingException {
        return getOrderUseCase.getAllOrders();
    }

    @GetMapping("/admin/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<OrderResponse> getAllOrdersByUserId(@PathVariable("id") Long userId) throws JsonProcessingException {
        return getOrderUseCase.getAllOrdersByUserId(userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public List<OrderResponse> getAllOrdersByUserId(@AuthenticationPrincipal AuthPrincipal authPrincipal) throws JsonProcessingException {
        return getOrderUseCase.getAllOrdersByUserId(authPrincipal.getUserId());
    }

    @GetMapping("/admin/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<OrderResponse> getAllOrdersByOrderStatus(@RequestParam("status") OrderStatus status) throws JsonProcessingException {
        return getOrderUseCase.getAllOrdersByOrderStatus(status);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public List<OrderResponse> getAllOrdersByOrderStatusAndUserId(@RequestParam("status") OrderStatus status,
                                                                  @AuthenticationPrincipal AuthPrincipal authPrincipal) throws JsonProcessingException {
        return getOrderUseCase.getAllOrdersByOrderStatus(status, authPrincipal.getUserId());
    }
    @GetMapping("/admin/createdAt")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<OrderResponse> getAllOrdersByCreatedAtAfter(@RequestParam("createdAt") OffsetDateTime createdAt) throws JsonProcessingException {
        return getOrderUseCase.getAllOrdersByCreatedAtAfter(createdAt);
    }

    @GetMapping("/createdAt")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public List<OrderResponse> getAllOrdersByCreatedAtAfterAndUserId(@RequestParam("createdAt") OffsetDateTime createdAt,
                                                                     @AuthenticationPrincipal AuthPrincipal authPrincipal) throws JsonProcessingException {
        return getOrderUseCase.getAllOrdersByCreatedAtAfter(createdAt, authPrincipal.getUserId());
    }


}
