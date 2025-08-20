package org.kuraterut.authservice.exception.model;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    public UserAlreadyExistsException(String format, Object... args) {
        super(String.format(format, args));
    }
}
