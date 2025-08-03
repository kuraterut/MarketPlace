package org.kuraterut.paymentservice.exception.model;

public class PaymentAccountIsNotEmptyException extends RuntimeException {
    public PaymentAccountIsNotEmptyException(String message) {
        super(message);
    }
}
