package com.plantogether.common.grpc;

import java.util.UUID;

public record TripMember(UUID deviceId, String displayName, Role role, String tripMemberId) {

  public TripMember(UUID deviceId, String displayName, Role role) {
    this(deviceId, displayName, role, null);
  }
}
