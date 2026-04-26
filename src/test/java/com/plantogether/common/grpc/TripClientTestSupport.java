package com.plantogether.common.grpc;

import java.util.List;

/**
 * Factory class providing pre-configured {@link InProcessTripClient} instances for unit tests
 * in consumer services.
 *
 * <p>Usage — single factory:
 * <pre>{@code
 * try (var client = TripClientTestSupport.member(TRIP_ID, DEVICE_ID)) {
 *     assertThat(client.isMember(TRIP_ID, DEVICE_ID)).isTrue();
 * }
 * }</pre>
 *
 * <p>Usage — builder (multiple configurations):
 * <pre>{@code
 * try (var client = TripClientTestSupport.builder()
 *         .member(TRIP_ID, DEVICE_ID)
 *         .withCurrency(TRIP_ID, "EUR")
 *         .withMembers(TRIP_ID, List.of(new TripMember(uuid, "Alice", Role.ORGANIZER)))
 *         .build()) {
 *     // test code
 * }
 * }</pre>
 */
public final class TripClientTestSupport {

    private TripClientTestSupport() {}

    /**
     * Returns a TripClient that reports the given device as a PARTICIPANT member of the given trip.
     * All other (tripId, deviceId) pairs return isMember=false.
     */
    public static InProcessTripClient member(String tripId, String deviceId) {
        return builder().member(tripId, deviceId).build();
    }

    /**
     * Returns a TripClient that reports the given device as an ORGANIZER of the given trip.
     * All other (tripId, deviceId) pairs return isMember=false.
     */
    public static InProcessTripClient organizer(String tripId, String deviceId) {
        return builder().organizer(tripId, deviceId).build();
    }

    /**
     * Returns a TripClient that denies membership for every call.
     * getTripCurrency returns "EUR" and getTripMembers returns an empty list for any tripId.
     */
    public static InProcessTripClient denyAll() {
        var service = new InProcessTripClient.ConfigurableTripService();
        service.setDenyAll();
        return new InProcessTripClient(service);
    }

    /**
     * Returns a TripClient pre-configured to answer getTripCurrency for the given tripId.
     * No members are pre-configured.
     */
    public static InProcessTripClient withCurrency(String tripId, String currency) {
        return builder().withCurrency(tripId, currency).build();
    }

    /**
     * Returns a TripClient pre-configured to answer getTripMembers for the given tripId.
     * No membership is pre-configured.
     */
    public static InProcessTripClient withMembers(String tripId, List<TripMember> memberList) {
        return builder().withMembers(tripId, memberList).build();
    }

    /** Returns a builder for composing multiple configurations into a single TripClient. */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private final InProcessTripClient.ConfigurableTripService service =
                new InProcessTripClient.ConfigurableTripService();

        private Builder() {}

        /** Registers the device as a PARTICIPANT member of the trip. */
        public Builder member(String tripId, String deviceId) {
            service.addMember(tripId, deviceId, Role.PARTICIPANT.name());
            return this;
        }

        /** Registers the device as an ORGANIZER of the trip. */
        public Builder organizer(String tripId, String deviceId) {
            service.addMember(tripId, deviceId, Role.ORGANIZER.name());
            return this;
        }

        /** Configures the currency returned for the given tripId. */
        public Builder withCurrency(String tripId, String currency) {
            service.setCurrency(tripId, currency);
            return this;
        }

        /** Configures the members returned for the given tripId. */
        public Builder withMembers(String tripId, List<TripMember> memberList) {
            service.setMembers(tripId, memberList);
            return this;
        }

        /** Builds and starts the in-process gRPC server, returning a ready-to-use TripClient. */
        public InProcessTripClient build() {
            return new InProcessTripClient(service);
        }
    }
}
