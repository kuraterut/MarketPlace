package org.kuraterut.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpResponse;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<Void> authServiceFallback(ServerWebExchange exchange) {
        return build503(exchange, "Auth Service is unavailable. Please try again later.");
    }

    @GetMapping("/product")
    public Mono<Void> productServiceFallback(ServerWebExchange exchange) {
        return build503(exchange, "Product Service is unavailable. Please try again later.");
    }

    @GetMapping("/order")
    public Mono<Void> orderServiceFallback(ServerWebExchange exchange) {
        return build503(exchange, "Order Service is unavailable. Please try again later.");
    }

    @GetMapping("/payment")
    public Mono<Void> paymentFallback(ServerWebExchange exchange) {
        return build503(exchange, "Payment Service is unavailable. Please try again later.");
    }

    private Mono<Void> build503(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}
