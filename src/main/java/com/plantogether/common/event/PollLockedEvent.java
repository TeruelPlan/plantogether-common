package com.plantogether.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollLockedEvent implements TripEvent {
    private String pollId;
    private String tripId;
    private String slotId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String lockedByDeviceId;
    private Instant occurredAt;
}
