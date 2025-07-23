package org.kuraterut.orderservice.usecases;

import java.util.concurrent.ExecutionException;

public interface PaymentProcessUseCase {
    void processPaymentEvents() throws ExecutionException, InterruptedException;
}
