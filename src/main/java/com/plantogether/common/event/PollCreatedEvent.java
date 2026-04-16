package com.plantogether.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollCreatedEvent implements TripEvent {
    private String pollId;
    private String tripId;
    private String createdByDeviceId;
    private String title;
    private int slotCount;
    private Instant createdAt;
}
