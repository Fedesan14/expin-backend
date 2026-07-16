package com.fedesan14.expin_backend.events.controller.responses;

import java.util.UUID;

import com.fedesan14.expin_backend.events.data.model.EventParticipant;
import com.fedesan14.expin_backend.events.data.enums.ParticipantType;

public record EventParticipantResponse(
	UUID id,
	UUID userId,
	String guestName,
	String displayName,
	ParticipantType type
) {

	public static EventParticipantResponse from(EventParticipant participant) {
		UUID userId = participant.getUser() == null ? null : participant.getUser().getId();
		return new EventParticipantResponse(
			participant.getId(),
			userId,
			participant.getGuestName(),
			participant.displayName(),
			participant.getType()
		);
	}
}
