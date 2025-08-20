package org.kuraterut.productservice.exception.model;

public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String message) {
        super(message);
    }
    public PermissionDeniedException(String format, Object... args) {
        super(String.format(format, args));
    }
}
