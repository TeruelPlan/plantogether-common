package com.plantogether.common.grpc;

import com.plantogether.common.exception.AccessDeniedException;
import com.plantogether.common.exception.ResourceNotFoundException;
import com.plantogether.trip.grpc.GetTripCurrencyRequest;
import com.plantogether.trip.grpc.GetTripMembersRequest;
import com.plantogether.trip.grpc.IsMemberRequest;
import com.plantogether.trip.grpc.TripServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Canonical gRPC implementation of {@link TripClient}.
 *
 * <p>Channel lifecycle is owned by {@link TripClientAutoConfiguration} — this class holds only the
 * stub and has no {@code @PostConstruct} or {@code @PreDestroy}.
 */
public class TripGrpcClient implements TripClient {

    private enum Operation {
        IS_MEMBER, REQUIRE_MEMBERSHIP, GET_CURRENCY, GET_MEMBERS
    }

    private final TripServiceGrpc.TripServiceBlockingStub stub;

    public TripGrpcClient(TripServiceGrpc.TripServiceBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public boolean isMember(String tripId, String deviceId) {
        try {
            var resp = stub.withDeadlineAfter(2, TimeUnit.SECONDS)
                    .isMember(IsMemberRequest.newBuilder()
                            .setTripId(tripId)
                            .setDeviceId(deviceId)
                            .build());
            return resp.getIsMember();
        } catch (StatusRuntimeException e) {
            throw handleStatusRuntimeException(e, Operation.IS_MEMBER, tripId);
        }
    }

    @Override
    public TripMembership requireMembership(String tripId, String deviceId) {
        try {
            var resp = stub.withDeadlineAfter(2, TimeUnit.SECONDS)
                    .isMember(IsMemberRequest.newBuilder()
                            .setTripId(tripId)
                            .setDeviceId(deviceId)
                            .build());
            if (!resp.getIsMember()) {
                throw new AccessDeniedException("Unable to verify trip membership");
            }
            return new TripMembership(true, Role.fromWire(resp.getRole()));
        } catch (StatusRuntimeException e) {
            throw handleStatusRuntimeException(e, Operation.REQUIRE_MEMBERSHIP, tripId);
        }
    }

    @Override
    public String getTripCurrency(String tripId) {
        try {
            var resp = stub.withDeadlineAfter(2, TimeUnit.SECONDS)
                    .getTripCurrency(GetTripCurrencyRequest.newBuilder()
                            .setTripId(tripId)
                            .build());
            return resp.getCurrencyCode();
        } catch (StatusRuntimeException e) {
            throw handleStatusRuntimeException(e, Operation.GET_CURRENCY, tripId);
        }
    }

    @Override
    public List<TripMember> getTripMembers(String tripId) {
        try {
            var resp = stub.withDeadlineAfter(2, TimeUnit.SECONDS)
                    .getTripMembers(GetTripMembersRequest.newBuilder()
                            .setTripId(tripId)
                            .build());
            return resp.getMembersList().stream()
                    .map(m -> new TripMember(
                            UUID.fromString(m.getDeviceId()),
                            m.getDisplayName(),
                            Role.fromWire(m.getRole())))
                    .toList();
        } catch (StatusRuntimeException e) {
            throw handleStatusRuntimeException(e, Operation.GET_MEMBERS, tripId);
        }
    }

    private RuntimeException handleStatusRuntimeException(StatusRuntimeException e, Operation op, String tripId) {
        Status.Code code = e.getStatus().getCode();
        return switch (code) {
            case UNAVAILABLE, DEADLINE_EXCEEDED ->
                    new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "trip-service unavailable", e);
            case INTERNAL, UNKNOWN ->
                    new ResponseStatusException(HttpStatus.BAD_GATEWAY, "trip-service error", e);
            case PERMISSION_DENIED, NOT_FOUND -> switch (op) {
                case IS_MEMBER, REQUIRE_MEMBERSHIP ->
                        new AccessDeniedException("Unable to verify trip membership");
                case GET_CURRENCY, GET_MEMBERS ->
                        new ResourceNotFoundException("Trip", tripId);
            };
            default ->
                    new ResponseStatusException(HttpStatus.BAD_GATEWAY, "trip-service unexpected error", e);
        };
    }
}
