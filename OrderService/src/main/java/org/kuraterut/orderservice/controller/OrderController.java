package org.kuraterut.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.kuraterut.orderservice.dto.CreateOrderRequest;
import org.kuraterut.orderservice.dto.OrderResponse;
import org.kuraterut.orderservice.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    //TODO Usecases
    //TODO Роли
    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request,
                                     @AuthenticationPrincipal AuthPrincipal authPrincipal){
        return orderService.createOrder(request, authPrincipal.getUserId());
    }
}
