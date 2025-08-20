package org.kuraterut.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.kuraterut.authservice.exception.model.UserAlreadyExistsException;
import org.kuraterut.authservice.model.utils.Role;
import org.kuraterut.authservice.model.entity.User;
import org.kuraterut.authservice.model.utils.UserDetailsImpl;
import org.kuraterut.authservice.dto.requests.RegisterRequest;
import org.kuraterut.authservice.dto.responses.RegisterResponse;
import org.kuraterut.authservice.repository.UserRepository;
import org.kuraterut.authservice.usecases.RegisterUseCase;
import org.kuraterut.paymentservice.grpc.CreateAccountRequest;
import org.kuraterut.paymentservice.grpc.CreateAccountResponse;
import org.kuraterut.paymentservice.grpc.PaymentServiceGrpc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterService implements RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtGeneratorService jwtGeneratorService;

//    @GrpcClient("payment-service")
    private final PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceBlockingStub;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        log.info("[RegisterService:register] Start registration");

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists: " + registerRequest.getEmail());
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .build();

        user = userRepository.save(user);
        log.info("[RegisterService:register] User saved: {}", user);

        if (registerRequest.getRole() != Role.ADMIN) {
            CreateAccountRequest grpcRequest = CreateAccountRequest.newBuilder()
                    .setUserId(user.getId())
                    .setEmail(user.getEmail())
                    .build();

            CreateAccountResponse grpcResponse = paymentServiceBlockingStub.createAccount(grpcRequest);

            if (!grpcResponse.getSuccess()) {
                throw new RuntimeException("Payment account creation failed for user " + user.getId());
            }

            log.info("[RegisterService:register] Payment account created: {}", grpcResponse.getAccountId());
        }

        String token = jwtGeneratorService.generateToken(
                new UserDetailsImpl(user.getEmail(), user.getPassword(), user.getId(), List.of(user.getRole()))
        );

        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setToken(token);

        log.info("[RegisterService:register] Registration success: {}", registerResponse);
        return registerResponse;
    }
}

