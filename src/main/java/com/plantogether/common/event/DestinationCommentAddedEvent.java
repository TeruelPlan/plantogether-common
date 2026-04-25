package com.plantogether.common.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Published by destination-service on routing key "destination.comment.added" when a member posts
 * a comment on a destination proposal. Payload carries identifiers only — the comment content is
 * never propagated through RabbitMQ. notification-service resolves display names and formats the
 * push on its side.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationCommentAddedEvent implements TripEvent {
  private UUID tripId;
  private UUID destinationId;
  private UUID commentId;
  private UUID authorDeviceId;
  private Instant occurredAt;
}
