package org.kuraterut.paymentservice.exception.model;

public class PaymentAccountIsNotEmptyException extends RuntimeException {
    public PaymentAccountIsNotEmptyException(String message) {
        super(message);
    }
    public PaymentAccountIsNotEmptyException(String format, Object... args) {
        super(String.format(format, args));
    }
}
