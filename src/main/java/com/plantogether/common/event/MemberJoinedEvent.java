package com.plantogether.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberJoinedEvent implements TripEvent {
    private UUID tripId;
    private String keycloakId;
    private Instant joinedAt;
}
