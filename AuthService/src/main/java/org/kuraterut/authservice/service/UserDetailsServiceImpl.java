package org.kuraterut.authservice.service;

import lombok.extern.slf4j.Slf4j;
import org.kuraterut.authservice.model.User;
import org.kuraterut.authservice.model.UserDetailsImpl;
import org.kuraterut.authservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user by email: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.info("User not found by email: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.info("User found: " + user.getEmail());
        return new UserDetailsImpl(user.getEmail(), user.getPassword(), user.getId(), List.of(user.getRole()));
    }
}