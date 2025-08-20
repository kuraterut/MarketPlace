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
import org.kuraterut.paymentservice.grpc.CreateAccountRequest;
import org.kuraterut.paymentservice.grpc.CreateAccountResponse;
import org.kuraterut.paymentservice.grpc.PaymentServiceGrpc;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationServiceUnitTest {

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

    @Mock
    private PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceStub;

    @InjectMocks
    private RegisterService registerService;


    @Test
    void register_success_nonAdmin() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("test@example.com", "pass", Role.CUSTOMER);
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded")
                .role(Role.CUSTOMER)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(jwtGeneratorService.generateToken(any())).thenReturn("jwt-token");

        // gRPC mock
        CreateAccountResponse grpcResponse = CreateAccountResponse.newBuilder()
                .setAccountId(1L)
                .setSuccess(true)
                .build();

        when(paymentServiceStub.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(grpcResponse);

        // when
        RegisterResponse response = registerService.register(request);

        // then
        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(paymentServiceStub).createAccount(any(CreateAccountRequest.class));
        verify(userRepository).save(any(User.class));
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
