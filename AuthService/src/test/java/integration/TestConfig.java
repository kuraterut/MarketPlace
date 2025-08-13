package integration;

import org.kuraterut.authservice.model.event.UserRegistrationEvent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, UserRegistrationEvent> kafkaTemplate() {
        KafkaTemplate<String, UserRegistrationEvent> template = mock(KafkaTemplate.class);
        when(template.send(anyString(), any(UserRegistrationEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));
        return template;
    }
}

