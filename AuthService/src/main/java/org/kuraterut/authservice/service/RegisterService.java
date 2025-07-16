package org.kuraterut.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.authservice.exception.model.UserAlreadyExistsException;
import org.kuraterut.authservice.model.User;
import org.kuraterut.authservice.model.UserDetailsImpl;
import org.kuraterut.authservice.model.dto.requests.RegisterRequest;
import org.kuraterut.authservice.model.dto.responses.RegisterResponse;
import org.kuraterut.authservice.repository.UserRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterService {
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtGeneratorService jwtGeneratorService;

    @Transactional
    @Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public RegisterResponse register(RegisterRequest registerRequest) {
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + registerRequest.getEmail());
        }
        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .build();

        user = userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user.getEmail(), user.getPassword(), user.getId(), List.of(user.getRole()));

        String token = jwtGeneratorService.generateToken(userDetails);
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setToken(token);

        return registerResponse;
    }
}
