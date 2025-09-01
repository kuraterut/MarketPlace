package integration;

import org.kuraterut.authservice.model.event.UserRegistrationEvent;
import org.kuraterut.paymentservice.grpc.CreateAccountRequest;
import org.kuraterut.paymentservice.grpc.CreateAccountResponse;
import org.kuraterut.paymentservice.grpc.PaymentServiceGrpc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, UserRegistrationEvent> kafkaTemplate() {
        KafkaTemplate<String, UserRegistrationEvent> template = mock(KafkaTemplate.class);
        when(template.send(anyString(), any(UserRegistrationEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        return template;
    }

    @Bean
    @Primary
    public PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceBlockingStub() {
        PaymentServiceGrpc.PaymentServiceBlockingStub stub = mock(PaymentServiceGrpc.PaymentServiceBlockingStub.class);

        CreateAccountResponse fakeResponse = CreateAccountResponse.newBuilder()
                .setAccountId(1L)
                .setSuccess(true)
                .build();

        when(stub.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(fakeResponse);

        return stub;
    }
}

