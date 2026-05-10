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
    // Legacy field — will be removed in Phase 3.
    private String deviceId;
    private String tripMemberId;
    private Instant joinedAt;
}
