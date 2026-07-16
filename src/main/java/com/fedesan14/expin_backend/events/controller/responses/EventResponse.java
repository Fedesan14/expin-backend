package com.fedesan14.expin_backend.events.controller.responses;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.enums.EventStatus;
import com.fedesan14.expin_backend.events.data.model.Event;

public record EventResponse(
	UUID id,
	String title,
	String description,
	LocalDate startDate,
	LocalDate endDate,
	String shareLink,
	UUID ownerId,
	List<EventParticipantResponse> participants,
	List<EventExpenseResponse> expenses,
    EventStatus status,
    EventSettlementResponse eventSettlementResponse
) {

	public static EventResponse from(Event event) {
		return new EventResponse(
			event.getId(),
			event.getTitle(),
			event.getDescription(),
			event.getStartDate(),
			event.getEndDate(),
			event.getShareLink(),
			event.getOwner().getId(),
			event.getParticipants().stream()
				.sorted(Comparator.comparing(participant -> participant.getId().toString()))
				.map(EventParticipantResponse::from)
				.toList(),
			event.getExpenses().stream()
				.sorted(Comparator.comparing(expense -> expense.getId().toString()))
				.map(EventExpenseResponse::from)
				.toList(),
            event.getStatus(),
            EventSettlementResponse.from(event.getSettlement())
		);
	}
}
