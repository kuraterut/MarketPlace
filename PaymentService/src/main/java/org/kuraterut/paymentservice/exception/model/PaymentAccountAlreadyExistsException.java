package org.kuraterut.paymentservice.exception.model;

public class PaymentAccountAlreadyExistsException extends RuntimeException {
    public PaymentAccountAlreadyExistsException(String message) {
        super(message);
    }
    public PaymentAccountAlreadyExistsException(String format, Object... args) {
        super(String.format(format, args));
    }
}
