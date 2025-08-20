package org.kuraterut.productservice.exception.model;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
    public CategoryAlreadyExistsException(String format, Object... args) {
        super(String.format(format, args));
    }
}
