package com.fedesan14.expin_backend.events.controller.responses;

import java.time.LocalDate;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.enums.EventStatus;
import com.fedesan14.expin_backend.events.data.model.Event;

public record EventSummaryResponse(
	UUID id,
	String title,
	String description,
	LocalDate startDate,
	LocalDate endDate,
	String shareLink,
	UUID ownerId,
    int participantsCount,
    EventStatus status
) {

	public static EventSummaryResponse from(Event event) {
		return new EventSummaryResponse(
			event.getId(),
			event.getTitle(),
			event.getDescription(),
			event.getStartDate(),
			event.getEndDate(),
			event.getShareLink(),
			event.getOwner().getId(),
            event.getParticipants().size(),
            event.getStatus()
		);
	}
}
