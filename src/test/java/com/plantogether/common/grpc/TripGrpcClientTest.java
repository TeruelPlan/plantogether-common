package com.plantogether.common.grpc;

import com.plantogether.common.exception.AccessDeniedException;
import com.plantogether.common.exception.ResourceNotFoundException;
import com.plantogether.trip.grpc.GetTripCurrencyRequest;
import com.plantogether.trip.grpc.GetTripCurrencyResponse;
import com.plantogether.trip.grpc.GetTripMembersRequest;
import com.plantogether.trip.grpc.GetTripMembersResponse;
import com.plantogether.trip.grpc.IsMemberRequest;
import com.plantogether.trip.grpc.IsMemberResponse;
import com.plantogether.trip.grpc.TripServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TripGrpcClientTest {

    private static final String TRIP_ID = UUID.randomUUID().toString();
    private static final String DEVICE_ID = UUID.randomUUID().toString();

    private Server server;
    private ManagedChannel channel;
    private TripGrpcClient client;
    private ControlledTripService fakeService;

    @BeforeEach
    void setUp() throws IOException {
        fakeService = new ControlledTripService();
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(fakeService)
                .build()
                .start();
        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        client = new TripGrpcClient(TripServiceGrpc.newBlockingStub(channel));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void isMember_member_returnsTrue() {
        fakeService.setIsMemberResult(true, "PARTICIPANT");

        assertThat(client.isMember(TRIP_ID, DEVICE_ID)).isTrue();
    }

    @Test
    void isMember_nonMember_returnsFalse() {
        fakeService.setIsMemberResult(false, "");

        assertThat(client.isMember(TRIP_ID, DEVICE_ID)).isFalse();
    }

    @Test
    void requireMembership_member_returnsTripMembershipWithRole() {
        fakeService.setIsMemberResult(true, "ORGANIZER");

        TripMembership membership = client.requireMembership(TRIP_ID, DEVICE_ID);

        assertThat(membership.isMember()).isTrue();
        assertThat(membership.role()).isEqualTo(Role.ORGANIZER);
    }

    @Test
    void requireMembership_nonMember_throwsAccessDenied() {
        fakeService.setIsMemberResult(false, "");

        assertThatThrownBy(() -> client.requireMembership(TRIP_ID, DEVICE_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("membership");
    }

    @Test
    void getTripCurrency_returnsCurrencyCode() {
        fakeService.setCurrency("EUR");

        assertThat(client.getTripCurrency(TRIP_ID)).isEqualTo("EUR");
    }

    @Test
    void getTripMembers_returnsListOfTripMembers() {
        UUID memberUuid = UUID.randomUUID();
        fakeService.setMembers(List.of(
                com.plantogether.trip.grpc.TripMemberProto.newBuilder()
                        .setDeviceId(memberUuid.toString())
                        .setDisplayName("Alice")
                        .setRole("ORGANIZER")
                        .build()));

        List<TripMember> members = client.getTripMembers(TRIP_ID);

        assertThat(members).hasSize(1);
        assertThat(members.get(0).deviceId()).isEqualTo(memberUuid);
        assertThat(members.get(0).displayName()).isEqualTo("Alice");
        assertThat(members.get(0).role()).isEqualTo(Role.ORGANIZER);
    }

    @Test
    void isMember_serverThrowsUnavailable_mapsTo503() {
        fakeService.setErrorStatus(Status.UNAVAILABLE);

        assertThatThrownBy(() -> client.isMember(TRIP_ID, DEVICE_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void isMember_serverThrowsDeadlineExceeded_mapsTo503() {
        fakeService.setErrorStatus(Status.DEADLINE_EXCEEDED);

        assertThatThrownBy(() -> client.isMember(TRIP_ID, DEVICE_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void isMember_serverThrowsInternal_mapsTo502() {
        fakeService.setErrorStatus(Status.INTERNAL);

        assertThatThrownBy(() -> client.isMember(TRIP_ID, DEVICE_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_GATEWAY));
    }

    @Test
    void isMember_serverThrowsPermissionDenied_mapsToAccessDenied() {
        fakeService.setErrorStatus(Status.PERMISSION_DENIED);

        assertThatThrownBy(() -> client.isMember(TRIP_ID, DEVICE_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getTripCurrency_serverThrowsNotFound_mapsToResourceNotFound() {
        fakeService.setErrorStatus(Status.NOT_FOUND);

        assertThatThrownBy(() -> client.getTripCurrency(TRIP_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /**
     * Regression test: gRPC INTERNAL/UNKNOWN must never become 403.
     * This was the bug caught in 3 consecutive epics (4.1, 4.2, 4.4).
     */
    @Test
    void isMember_serverThrowsUnknownCode_mapsTo502_notForbidden() {
        fakeService.setErrorStatus(Status.UNKNOWN);

        assertThatThrownBy(() -> client.isMember(TRIP_ID, DEVICE_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    var statusCode = ((ResponseStatusException) ex).getStatusCode();
                    assertThat(statusCode).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(statusCode).isNotEqualTo(HttpStatus.FORBIDDEN);
                });
    }

    // -------------------------------------------------------------------------
    // Controlled fake service for direct error-path testing
    // -------------------------------------------------------------------------

    private static class ControlledTripService extends TripServiceGrpc.TripServiceImplBase {

        private boolean isMember;
        private String role;
        private String currency;
        private List<com.plantogether.trip.grpc.TripMemberProto> memberList;
        private Status errorStatus;

        void setIsMemberResult(boolean isMember, String role) {
            this.isMember = isMember;
            this.role = role;
            this.errorStatus = null;
        }

        void setCurrency(String currency) {
            this.currency = currency;
            this.errorStatus = null;
        }

        void setMembers(List<com.plantogether.trip.grpc.TripMemberProto> members) {
            this.memberList = members;
            this.errorStatus = null;
        }

        void setErrorStatus(Status status) {
            this.errorStatus = status;
        }

        @Override
        public void isMember(IsMemberRequest request, StreamObserver<IsMemberResponse> observer) {
            if (errorStatus != null) {
                observer.onError(errorStatus.asRuntimeException());
                return;
            }
            observer.onNext(IsMemberResponse.newBuilder().setIsMember(isMember).setRole(role).build());
            observer.onCompleted();
        }

        @Override
        public void getTripCurrency(GetTripCurrencyRequest request,
                StreamObserver<GetTripCurrencyResponse> observer) {
            if (errorStatus != null) {
                observer.onError(errorStatus.asRuntimeException());
                return;
            }
            observer.onNext(GetTripCurrencyResponse.newBuilder().setCurrencyCode(currency).build());
            observer.onCompleted();
        }

        @Override
        public void getTripMembers(GetTripMembersRequest request,
                StreamObserver<GetTripMembersResponse> observer) {
            if (errorStatus != null) {
                observer.onError(errorStatus.asRuntimeException());
                return;
            }
            observer.onNext(GetTripMembersResponse.newBuilder().addAllMembers(memberList).build());
            observer.onCompleted();
        }
    }
}
