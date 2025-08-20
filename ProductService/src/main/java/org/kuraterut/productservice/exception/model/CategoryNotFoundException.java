package org.kuraterut.productservice.exception.model;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
    public CategoryNotFoundException(String format, Object... args) {
        super(String.format(format, args));
    }
}
