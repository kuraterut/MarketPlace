package org.kuraterut.bankaccount.exception.model;

public class BankAccountIsNotEmptyException extends RuntimeException {
    public BankAccountIsNotEmptyException(String message) {
        super(message);
    }
}
