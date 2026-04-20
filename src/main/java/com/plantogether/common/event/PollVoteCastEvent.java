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
public class PollVoteCastEvent implements TripEvent {
    private String pollId;
    private String tripId;
    private String slotId;
    private String deviceId;
    private String status;
    private int newSlotScore;
    private Instant occurredAt;
}
