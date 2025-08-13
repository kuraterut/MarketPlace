package unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuraterut.authservice.dto.requests.LoginRequest;
import org.kuraterut.authservice.dto.responses.LoginResponse;
import org.kuraterut.authservice.exception.model.UserNotFoundException;
import org.kuraterut.authservice.model.entity.User;
import org.kuraterut.authservice.model.utils.Role;
import org.kuraterut.authservice.model.utils.UserDetailsImpl;
import org.kuraterut.authservice.repository.UserRepository;
import org.kuraterut.authservice.service.JwtGeneratorService;
import org.kuraterut.authservice.service.LoginService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginServiceUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtGeneratorService jwtGeneratorService;

    @InjectMocks
    private LoginService loginService;

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        User user = User.builder().id(1L).email("test@example.com").password("encoded").role(Role.ADMIN).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(user.getEmail(), user.getPassword(), user.getId(), List.of(user.getRole()));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtGeneratorService.generateToken(userDetails)).thenReturn("jwt-token");

        LoginResponse response = loginService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_userNotFound_throws() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("test@example.com", "password");

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(UserNotFoundException.class);
    }
}
