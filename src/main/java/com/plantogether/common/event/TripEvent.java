package com.plantogether.common.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TripCreatedEvent.class, name = "TRIP_CREATED"),
        @JsonSubTypes.Type(value = TripDeletedEvent.class, name = "TRIP_DELETED"),
        @JsonSubTypes.Type(value = MemberJoinedEvent.class, name = "MEMBER_JOINED"),
})
public interface TripEvent {
}
