package com.plantogether.common.grpc;

public record TripMembership(boolean isMember, Role role, String tripMemberId) {

  public TripMembership(boolean isMember, Role role) {
    this(isMember, role, null);
  }
}
