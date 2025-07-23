package org.kuraterut.authservice.usecases;

import org.kuraterut.authservice.dto.requests.LoginRequest;
import org.kuraterut.authservice.dto.responses.LoginResponse;

public interface LoginUseCase {
    LoginResponse login(LoginRequest request);
}
