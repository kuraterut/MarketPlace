package org.kuraterut.orderservice.exception.model;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
    public OrderNotFoundException(String format, Object... args) {
        super(String.format(format, args));
    }
}
