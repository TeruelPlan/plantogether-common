package com.plantogether.common.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignedEvent {
  private UUID taskId;
  private UUID tripId;
  private String assigneeMemberId;
  private String title;
  private Instant deadline;
  private Instant assignedAt;
}
