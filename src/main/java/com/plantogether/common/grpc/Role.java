package com.plantogether.common.grpc;

public enum Role {
    ORGANIZER,
    PARTICIPANT,
    NONE;

    /** Maps the wire-format string from IsMemberResponse.role to a Role. Returns NONE on null, empty, or unknown values. */
    public static Role fromWire(String s) {
        if (s == null || s.isBlank()) {
            return NONE;
        }
        return switch (s.toUpperCase()) {
            case "ORGANIZER" -> ORGANIZER;
            case "PARTICIPANT" -> PARTICIPANT;
            default -> NONE;
        };
    }
}
