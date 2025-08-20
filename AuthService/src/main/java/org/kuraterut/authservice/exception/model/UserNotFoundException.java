package org.kuraterut.authservice.exception.model;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException(String format, Object... args) {
        super(String.format(format, args));
    }
}
