package com.plantogether.common.grpc;

import com.plantogether.trip.grpc.TripServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * Auto-configures the shared {@link TripClient} gRPC bean.
 *
 * <p>Properties:
 * <ul>
 *   <li>{@code grpc.trip-service.host} (required) — hostname of trip-service gRPC endpoint</li>
 *   <li>{@code grpc.trip-service.port} (default 9081) — port of trip-service gRPC endpoint</li>
 * </ul>
 *
 * <p>The auto-configuration is inert unless {@code grpc.trip-service.host} is set, so services
 * that do not call trip-service (e.g. file-service) are unaffected.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "grpc.trip-service", name = "host")
@ConditionalOnClass(TripServiceGrpc.class)
public class TripClientAutoConfiguration {

    private ManagedChannel tripServiceChannel;

    @Bean
    @ConditionalOnMissingBean
    public ManagedChannel tripServiceChannel(
            @Value("${grpc.trip-service.host}") String host,
            @Value("${grpc.trip-service.port:9081}") int port) {
        this.tripServiceChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        return tripServiceChannel;
    }

    @PreDestroy
    void shutdownTripServiceChannel() throws InterruptedException {
        if (tripServiceChannel != null && !tripServiceChannel.isShutdown()) {
            tripServiceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public TripServiceGrpc.TripServiceBlockingStub tripServiceBlockingStub(ManagedChannel tripServiceChannel) {
        return TripServiceGrpc.newBlockingStub(tripServiceChannel);
    }

    @Bean
    @ConditionalOnMissingBean(TripClient.class)
    public TripClient tripClient(TripServiceGrpc.TripServiceBlockingStub tripServiceBlockingStub) {
        return new TripGrpcClient(tripServiceBlockingStub);
    }
}
