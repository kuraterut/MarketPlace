package org.kuraterut.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kuraterut.jwtsecuritylib.model.AuthPrincipal;
import org.kuraterut.orderservice.dto.CreateOrderRequest;
import org.kuraterut.orderservice.dto.OrderResponse;
import org.kuraterut.orderservice.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    //TODO Usecases
    //TODO Роли
    //TODO Обработка ошибок
    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request,
                                     @AuthenticationPrincipal AuthPrincipal authPrincipal) throws JsonProcessingException {
        return orderService.createOrder(request, authPrincipal.getUserId());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable("id") Long id) throws JsonProcessingException {
        return orderService.getOrder(id);
    }
}
