package unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuraterut.authservice.dto.requests.RegisterRequest;
import org.kuraterut.authservice.dto.responses.RegisterResponse;
import org.kuraterut.authservice.exception.model.UserAlreadyExistsException;
import org.kuraterut.authservice.model.entity.User;
import org.kuraterut.authservice.model.event.UserRegistrationEvent;
import org.kuraterut.authservice.model.utils.Role;
import org.kuraterut.authservice.repository.UserRepository;
import org.kuraterut.authservice.service.JwtGeneratorService;
import org.kuraterut.authservice.service.RegisterService;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RegistrationServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtGeneratorService jwtGeneratorService;
    @Mock
    private KafkaTemplate<String, UserRegistrationEvent> kafkaTemplate;

    @InjectMocks
    private RegisterService registerService;


    @BeforeEach
    void setup() {
        // подставляем имя топика, чтобы не было null
        ReflectionTestUtils.setField(registerService, "userRegistrationTopic", "test-topic");
    }

    @Test
    void register_success_nonAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "pass", Role.CUSTOMER);
        User user = User.builder().id(1L).email("test@example.com").password("encoded").role(Role.CUSTOMER).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(jwtGeneratorService.generateToken(any())).thenReturn("jwt-token");

        when(kafkaTemplate.send(anyString(), any(UserRegistrationEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));



        RegisterResponse response = registerService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(kafkaTemplate).send(anyString(), any(UserRegistrationEvent.class));
    }

    @Test
    void register_userAlreadyExists_throws() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));
        RegisterRequest request = new RegisterRequest("test@example.com", "pass", Role.ADMIN);

        assertThatThrownBy(() -> registerService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void register_success_admin_doesNotSendKafka() throws Exception {
        RegisterRequest request = new RegisterRequest("admin@example.com", "pass", Role.ADMIN);
        User user = User.builder().id(1L).email("admin@example.com").password("encoded").role(Role.ADMIN).build();
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(jwtGeneratorService.generateToken(any())).thenReturn("jwt-token");

        RegisterResponse response = registerService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verifyNoInteractions(kafkaTemplate);
    }
}
