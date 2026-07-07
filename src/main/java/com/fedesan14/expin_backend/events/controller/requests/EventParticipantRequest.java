package com.fedesan14.expin_backend.events.controller.requests;

import java.util.UUID;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record EventParticipantRequest(
	UUID userId,

	@Size(max = 255)
	String guestName
) {

	@AssertTrue(message = "Participant must have either userId or guestName")
	public boolean hasOneIdentity() {
		boolean hasUserId = userId != null;
		boolean hasGuestName = guestName != null && !guestName.isBlank();
		return hasUserId ^ hasGuestName;
	}
}
