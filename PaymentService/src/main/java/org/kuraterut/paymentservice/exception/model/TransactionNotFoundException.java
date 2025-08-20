package org.kuraterut.paymentservice.exception.model;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
    public TransactionNotFoundException(String format, Object... args) {
        super(String.format(format, args));
    }
}
