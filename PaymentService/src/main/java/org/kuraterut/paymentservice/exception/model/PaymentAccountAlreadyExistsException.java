package org.kuraterut.paymentservice.exception.model;

public class PaymentAccountAlreadyExistsException extends RuntimeException {
    public PaymentAccountAlreadyExistsException(String message) {
        super(message);
    }
}
