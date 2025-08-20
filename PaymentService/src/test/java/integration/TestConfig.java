package integration;

import net.devh.boot.grpc.server.config.GrpcServerProperties;
import net.devh.boot.grpc.server.service.GrpcServiceDiscoverer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestConfig {


    @Bean
    @Primary
    public GrpcServerProperties grpcServerProperties() {
        GrpcServerProperties properties = mock(GrpcServerProperties.class);
        // You might need to set some basic properties if the mock causes issues
        return properties;
    }

    @Bean
    @Primary
    public Object grpcServiceDefinition() {
        return mock(Object.class);
    }

    @Bean
    @Primary
    public Object grpcServiceDiscoverer() {
        return mock(Object.class);
    }

    @Bean
    @Primary
    public Object grpcChannelFactory() {
        return mock(Object.class);
    }
}
