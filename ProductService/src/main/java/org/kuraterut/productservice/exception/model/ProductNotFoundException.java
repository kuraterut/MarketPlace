package org.kuraterut.productservice.exception.model;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
    public ProductNotFoundException(String format, Object... args) {
        super(String.format(format, args));
    }
}
