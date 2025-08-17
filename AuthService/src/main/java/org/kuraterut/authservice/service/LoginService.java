package org.kuraterut.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kuraterut.authservice.exception.model.UserNotFoundException;
import org.kuraterut.authservice.model.utils.UserDetailsImpl;
import org.kuraterut.authservice.dto.requests.LoginRequest;
import org.kuraterut.authservice.dto.responses.LoginResponse;
import org.kuraterut.authservice.repository.UserRepository;
import org.kuraterut.authservice.usecases.LoginUseCase;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService implements LoginUseCase {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtGeneratorService jwtGeneratorService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("[LoginService:login] Login request: {}", request);
        if (userRepository.findByEmail(request.getEmail()).isEmpty()) {
            log.warn("[LoginService:login] User with email [{}] not found", request.getEmail());
            throw new UserNotFoundException("User not found by email: " + request.getEmail());
        }
        log.info("[LoginService:login] Start authentication");
        log.info("[LoginService:login] Encoded request password: {}", passwordEncoder.encode(request.getPassword()));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        ));
        log.info("[LoginService:login] Load User By Username");
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(request.getEmail());

        log.info("[LoginService:login] Generate Token");

        String token = jwtGeneratorService.generateToken(userDetails);
        return new LoginResponse(token);
    }
}
