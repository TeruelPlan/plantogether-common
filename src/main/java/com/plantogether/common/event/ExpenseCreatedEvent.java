package com.plantogether.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCreatedEvent {
    private UUID expenseId;
    private UUID tripId;
    private String paidByKeycloakId;
    private BigDecimal amount;
    private String description;
    private Instant createdAt;
}
