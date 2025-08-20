package org.kuraterut.paymentservice.exception.model;

public class PaymentAccountNotFoundException extends RuntimeException {
    public PaymentAccountNotFoundException(String message) {
        super(message);
    }
    public PaymentAccountNotFoundException(String format, Object... args) {
        super(String.format(format, args));
    }
}
