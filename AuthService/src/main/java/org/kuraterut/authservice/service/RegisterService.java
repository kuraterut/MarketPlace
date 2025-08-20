package org.kuraterut.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.authservice.exception.model.UserAlreadyExistsException;
import org.kuraterut.authservice.model.utils.Role;
import org.kuraterut.authservice.model.entity.User;
import org.kuraterut.authservice.model.utils.UserDetailsImpl;
import org.kuraterut.authservice.dto.requests.RegisterRequest;
import org.kuraterut.authservice.dto.responses.RegisterResponse;
import org.kuraterut.authservice.model.event.UserRegistrationEvent;
import org.kuraterut.authservice.repository.UserRepository;
import org.kuraterut.authservice.usecases.RegisterUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterService implements RegisterUseCase {
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtGeneratorService jwtGeneratorService;
    private final KafkaTemplate<String, UserRegistrationEvent> userRegistrationEventKafkaTemplate;

    @Value("${kafka-topics.user-registration}")
    private String userRegistrationTopic;

    @Override
    @Transactional
    @Retryable(retryFor = OptimisticLockingFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public RegisterResponse register(RegisterRequest registerRequest) throws ExecutionException, InterruptedException {
        log.info("[RegisterService:register] Start registration");
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.info("[RegisterService:register] User already exists with email {}", registerRequest.getEmail());
            throw new UserAlreadyExistsException("User already exists with email: " + registerRequest.getEmail());
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .build();

        user = userRepository.save(user);

        log.info("[RegisterService:register] User created and saved: {}", user);

        if(registerRequest.getRole() != Role.ADMIN) {
            userRegistrationEventKafkaTemplate.send(userRegistrationTopic, new UserRegistrationEvent(user.getId())).get();
            log.info("[RegisterService:register] User registration event was sent to message broker");
        }

        UserDetailsImpl userDetails = new UserDetailsImpl(user.getEmail(), user.getPassword(), user.getId(), List.of(user.getRole()));

        String token = jwtGeneratorService.generateToken(userDetails);
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setToken(token);
        log.info("[RegisterService:register] User registration success: {}", registerResponse);
        return registerResponse;
    }
}
