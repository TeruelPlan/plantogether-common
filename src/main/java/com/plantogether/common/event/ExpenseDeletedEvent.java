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
public class ExpenseDeletedEvent {
    private UUID expenseId;
    private UUID tripId;
    private UUID paidByMemberId;
    private UUID deletedByMemberId;
    private Instant deletedAt;
}
