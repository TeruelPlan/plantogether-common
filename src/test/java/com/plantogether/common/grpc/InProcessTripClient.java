package com.plantogether.common.grpc;

import com.plantogether.trip.grpc.GetTripCurrencyRequest;
import com.plantogether.trip.grpc.GetTripCurrencyResponse;
import com.plantogether.trip.grpc.GetTripMembersRequest;
import com.plantogether.trip.grpc.GetTripMembersResponse;
import com.plantogether.trip.grpc.IsMemberRequest;
import com.plantogether.trip.grpc.IsMemberResponse;
import com.plantogether.trip.grpc.TripMemberProto;
import com.plantogether.trip.grpc.TripServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * AutoCloseable in-process gRPC-backed TripClient for use in consumer service tests.
 *
 * <p>Use {@link TripClientTestSupport} factories or {@link TripClientTestSupport#builder()} to
 * obtain instances. Call {@link #close()} in {@code @AfterEach} or try-with-resources.
 *
 * <p>{@link #recordedCalls()} exposes received {@link IsMemberRequest}s for assertion.
 */
public class InProcessTripClient implements TripClient, AutoCloseable {

    private final Server server;
    private final ManagedChannel channel;
    private final TripGrpcClient delegate;
    final ConfigurableTripService configuredService;

    InProcessTripClient(ConfigurableTripService service) {
        this.configuredService = service;
        String serverName = InProcessServerBuilder.generateName();
        try {
            this.server = InProcessServerBuilder.forName(serverName)
                    .directExecutor()
                    .addService(service)
                    .build()
                    .start();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start in-process gRPC server", e);
        }
        this.channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        this.delegate = new TripGrpcClient(TripServiceGrpc.newBlockingStub(channel));
    }

    /** Returns all IsMemberRequests received since this client was created, in order. */
    public List<IsMemberRequest> recordedCalls() {
        return Collections.unmodifiableList(configuredService.recordedCalls);
    }

    @Override
    public boolean isMember(String tripId, String deviceId) {
        return delegate.isMember(tripId, deviceId);
    }

    @Override
    public TripMembership requireMembership(String tripId, String deviceId) {
        return delegate.requireMembership(tripId, deviceId);
    }

    @Override
    public String getTripCurrency(String tripId) {
        return delegate.getTripCurrency(tripId);
    }

    @Override
    public List<TripMember> getTripMembers(String tripId) {
        return delegate.getTripMembers(tripId);
    }

    @Override
    public void close() throws InterruptedException {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    // -------------------------------------------------------------------------
    // Configurable in-process trip-service implementation
    // -------------------------------------------------------------------------

    static class ConfigurableTripService extends TripServiceGrpc.TripServiceImplBase {

        /** Key: "tripId:deviceId" */
        private final Map<String, String> memberRoles = new HashMap<>();
        private final Map<String, String> currencies = new HashMap<>();
        private final Map<String, List<TripMemberProto>> members = new HashMap<>();
        private boolean denyAll = false;
        final List<IsMemberRequest> recordedCalls = new CopyOnWriteArrayList<>();

        void addMember(String tripId, String deviceId, String role) {
            memberRoles.put(tripId + ":" + deviceId, role);
        }

        void setCurrency(String tripId, String currency) {
            currencies.put(tripId, currency);
        }

        void setMembers(String tripId, List<TripMember> memberList) {
            List<TripMemberProto> protos = new ArrayList<>();
            for (TripMember m : memberList) {
                protos.add(TripMemberProto.newBuilder()
                        .setDeviceId(m.deviceId().toString())
                        .setDisplayName(m.displayName())
                        .setRole(m.role().name())
                        .build());
            }
            members.put(tripId, protos);
        }

        void setDenyAll() {
            this.denyAll = true;
        }

        @Override
        public void isMember(IsMemberRequest request, StreamObserver<IsMemberResponse> observer) {
            recordedCalls.add(request);
            if (denyAll) {
                observer.onNext(IsMemberResponse.newBuilder().setIsMember(false).setRole("").build());
                observer.onCompleted();
                return;
            }
            String key = request.getTripId() + ":" + request.getDeviceId();
            String role = memberRoles.get(key);
            if (role != null) {
                observer.onNext(IsMemberResponse.newBuilder().setIsMember(true).setRole(role).build());
            } else {
                observer.onNext(IsMemberResponse.newBuilder().setIsMember(false).setRole("").build());
            }
            observer.onCompleted();
        }

        @Override
        public void getTripCurrency(GetTripCurrencyRequest request,
                StreamObserver<GetTripCurrencyResponse> observer) {
            if (denyAll) {
                observer.onNext(GetTripCurrencyResponse.newBuilder().setCurrencyCode("EUR").build());
                observer.onCompleted();
                return;
            }
            String currency = currencies.get(request.getTripId());
            if (currency == null) {
                observer.onError(Status.NOT_FOUND.withDescription("trip not found").asRuntimeException());
            } else {
                observer.onNext(GetTripCurrencyResponse.newBuilder().setCurrencyCode(currency).build());
                observer.onCompleted();
            }
        }

        @Override
        public void getTripMembers(GetTripMembersRequest request,
                StreamObserver<GetTripMembersResponse> observer) {
            if (denyAll) {
                observer.onNext(GetTripMembersResponse.newBuilder().build());
                observer.onCompleted();
                return;
            }
            List<TripMemberProto> memberList = members.get(request.getTripId());
            if (memberList == null) {
                observer.onError(Status.NOT_FOUND.withDescription("trip not found").asRuntimeException());
            } else {
                observer.onNext(GetTripMembersResponse.newBuilder().addAllMembers(memberList).build());
                observer.onCompleted();
            }
        }
    }
}
