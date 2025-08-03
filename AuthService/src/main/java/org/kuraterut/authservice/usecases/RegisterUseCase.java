package org.kuraterut.authservice.usecases;

import org.kuraterut.authservice.dto.requests.RegisterRequest;
import org.kuraterut.authservice.dto.responses.RegisterResponse;

import java.util.concurrent.ExecutionException;

public interface RegisterUseCase {
    RegisterResponse register(RegisterRequest registerRequest) throws ExecutionException, InterruptedException;
}
