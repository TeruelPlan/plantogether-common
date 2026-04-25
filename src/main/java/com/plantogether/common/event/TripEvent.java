package com.plantogether.common.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TripCreatedEvent.class, name = "TRIP_CREATED"),
        @JsonSubTypes.Type(value = TripDeletedEvent.class, name = "TRIP_DELETED"),
        @JsonSubTypes.Type(value = MemberJoinedEvent.class, name = "MEMBER_JOINED"),
        @JsonSubTypes.Type(value = PollCreatedEvent.class, name = "POLL_CREATED"),
        @JsonSubTypes.Type(value = PollVoteCastEvent.class, name = "POLL_VOTE_CAST"),
        @JsonSubTypes.Type(value = PollLockedEvent.class, name = "POLL_LOCKED"),
        @JsonSubTypes.Type(value = VoteCastEvent.class, name = "VOTE_CAST"),
        @JsonSubTypes.Type(value = DestinationCommentAddedEvent.class, name = "DESTINATION_COMMENT_ADDED"),
})
public interface TripEvent {
}
