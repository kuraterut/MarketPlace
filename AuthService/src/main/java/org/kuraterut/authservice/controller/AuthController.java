package org.kuraterut.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.authservice.dto.requests.LoginRequest;
import org.kuraterut.authservice.dto.requests.RegisterRequest;
import org.kuraterut.authservice.dto.responses.LoginResponse;
import org.kuraterut.authservice.dto.responses.RegisterResponse;
import org.kuraterut.authservice.service.LoginService;
import org.kuraterut.authservice.service.RegisterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication Controller", description = "Controller for user authentication")
//TODO Написать интерфейсы к контроллерам
public class AuthController {
    private final LoginService loginService;
    private final RegisterService registerService;

    @PostMapping("/register")
    @Operation(summary = "User Registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registration successful, return token"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "User is already exists"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<RegisterResponse> register(
            @Parameter(description = "Registration request") @RequestBody @Valid RegisterRequest registerRequest) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(registerService.register(registerRequest));
    }

    @PostMapping("/login")
    @Operation(summary = "User Log in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User login successful, return token"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Login request") @RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(loginService.login(loginRequest));
    }
}
