package com.plantogether.common.event;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationChosenEvent implements TripEvent {
    private String tripId;
    private String destinationId;
    private String destinationName;
    private String chosenByDeviceId;
    private Instant chosenAt;
    private String previousChosenDestinationId;
    private Instant occurredAt;
}
