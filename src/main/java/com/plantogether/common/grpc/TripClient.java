package com.plantogether.common.grpc;

import com.plantogether.common.exception.AccessDeniedException;
import com.plantogether.common.exception.ResourceNotFoundException;

import java.util.List;

/**
 * Canonical gRPC client interface for trip-service operations.
 *
 * <p>Auto-configured by {@link TripClientAutoConfiguration} when {@code grpc.trip-service.host} is set.
 * Consumer services inject this interface directly — no gRPC client code required.
 */
public interface TripClient {

    /**
     * Returns true if the given device is a member of the given trip.
     *
     * @param tripId   UUID of the trip
     * @param deviceId UUID of the device
     * @return true if member, false otherwise
     */
    boolean isMember(String tripId, String deviceId);

    /**
     * Verifies membership and returns membership details.
     *
     * @param tripId   UUID of the trip
     * @param deviceId UUID of the device
     * @return TripMembership with role
     * @throws AccessDeniedException if the device is not a member
     */
    TripMembership requireMembership(String tripId, String deviceId);

    /**
     * Returns the ISO 4217 currency code configured for the trip.
     *
     * @param tripId UUID of the trip
     * @return currency code (e.g. "EUR")
     * @throws ResourceNotFoundException if the trip does not exist
     */
    String getTripCurrency(String tripId);

    /**
     * Returns all members of the trip with their display names and roles.
     *
     * @param tripId UUID of the trip
     * @return list of trip members; empty list if the trip has no members
     * @throws ResourceNotFoundException if the trip does not exist
     */
    List<TripMember> getTripMembers(String tripId);
}
