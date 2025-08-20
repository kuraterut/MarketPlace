package org.kuraterut.paymentservice.grpc;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.kuraterut.paymentservice.dto.response.PaymentAccountResponse;
import org.kuraterut.paymentservice.service.PaymentAccountService;

@GrpcService
@Slf4j
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {

    private final PaymentAccountService paymentAccountService;

    public PaymentGrpcService(PaymentAccountService paymentAccountService) {
        this.paymentAccountService = paymentAccountService;
    }

    @Override
    public void createAccount(CreateAccountRequest request,
                              StreamObserver<CreateAccountResponse> responseObserver) {
        log.info("Creating payment account for user {}", request.getUserId());

        // бизнес-логика — создаём аккаунт
        PaymentAccountResponse creationResponse = paymentAccountService.createPaymentAccount(request.getUserId());

        CreateAccountResponse response = CreateAccountResponse.newBuilder()
                .setAccountId(creationResponse.getId())
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

