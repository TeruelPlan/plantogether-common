package com.plantogether.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Published by destination-service on routing key "vote.cast" when a member
 * casts a vote on a destination proposal. Mirrors {@link PollVoteCastEvent}
 * but scoped to destination polls.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteCastEvent implements TripEvent {
    private String tripId;
    private String destinationId;
    private String deviceId;
    /** SIMPLE | APPROVAL | RANKING */
    private String voteMode;
    /** YES/NO for SIMPLE, score for RANKING, positional value for APPROVAL */
    private String voteValue;
    private Instant occurredAt;
}
