package org.kuraterut.authservice.config;

import io.grpc.Channel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.kuraterut.paymentservice.grpc.PaymentServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class GrpcConfig {

    private final Channel channel;

    // Use constructor injection instead of field injection
    public GrpcConfig(@GrpcClient("payment-service") Channel channel) {
        this.channel = channel;
    }

    @Bean
    @Profile("!test")
    public PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceStub() {
        return PaymentServiceGrpc.newBlockingStub(channel);
    }
}
