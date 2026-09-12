package vn.ktt.eartraining.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.ImportGrpcClients;
import vn.ktt.grpc.sound.SoundServiceGrpc;

@Configuration
@ImportGrpcClients(types = {
        SoundServiceGrpc.SoundServiceBlockingStub.class,
        SoundServiceGrpc.SoundServiceStub.class,
        SoundServiceGrpc.SoundServiceFutureStub.class
})
public class GrpcClientConfiguration {
}
