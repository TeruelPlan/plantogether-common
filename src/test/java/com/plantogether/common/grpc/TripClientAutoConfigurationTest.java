package com.plantogether.common.grpc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class TripClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TripClientAutoConfiguration.class));

    @Test
    void autoConfig_enabled_whenHostPropertySet_registersTripClientBean() {
        contextRunner
                .withPropertyValues("grpc.trip-service.host=localhost", "grpc.trip-service.port=9081")
                .run(context -> assertThat(context).hasSingleBean(TripClient.class));
    }

    @Test
    void autoConfig_disabled_whenHostPropertyMissing_noTripClientBean() {
        contextRunner
                .run(context -> assertThat(context).doesNotHaveBean(TripClient.class));
    }

    @Test
    void autoConfig_respectsConsumerOverride_whenAppDefinesTripClientBean() {
        contextRunner
                .withPropertyValues("grpc.trip-service.host=localhost")
                .withUserConfiguration(CustomTripClientConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(TripClient.class);
                    assertThat(context.getBean(TripClient.class))
                            .isInstanceOf(CustomTripClientConfig.NoopTripClient.class);
                });
    }

    @Configuration
    static class CustomTripClientConfig {
        @Bean
        TripClient tripClient() {
            return new NoopTripClient();
        }

        static class NoopTripClient implements TripClient {
            @Override public boolean isMember(String t, String d) { return false; }
            @Override public TripMembership requireMembership(String t, String d) { return new TripMembership(false, Role.NONE); }
            @Override public String getTripCurrency(String t) { return "USD"; }
            @Override public java.util.List<TripMember> getTripMembers(String t) { return java.util.List.of(); }
        }
    }
}
