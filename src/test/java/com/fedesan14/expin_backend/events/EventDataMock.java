package com.fedesan14.expin_backend.events;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fedesan14.expin_backend.events.controller.requests.CreateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventRequest;
import com.fedesan14.expin_backend.events.controller.requests.EventParticipantRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventRequest;

public final class EventDataMock {

	private EventDataMock() {
	}

	public static CreateEventRequest eventWithGuest(String prefix) {
		return new CreateEventRequest(
			"Trip " + uniqueToken(prefix),
			"Shared trip expenses",
			LocalDate.of(2026, 7, 10),
			LocalDate.of(2026, 7, 15),
			List.of(new EventParticipantRequest(null, "Guest " + uniqueToken(prefix)))
		);
	}

	public static CreateEventRequest eventWithUser(String prefix, UUID userId) {
		return new CreateEventRequest(
			"Shared event " + uniqueToken(prefix),
			"Shared expenses",
			LocalDate.of(2026, 7, 10),
			LocalDate.of(2026, 7, 15),
			List.of(new EventParticipantRequest(userId, null))
		);
	}

	public static UpdateEventRequest updateWithGuest(String prefix) {
		return new UpdateEventRequest(
			"Updated event " + uniqueToken(prefix),
			"Updated shared expenses",
			LocalDate.of(2026, 7, 10),
			LocalDate.of(2026, 7, 16),
			List.of(new EventParticipantRequest(null, "Updated Guest " + uniqueToken(prefix)))
		);
	}

	public static CreateEventExpenseRequest expense(UUID paidByParticipantId) {
		return expense(paidByParticipantId, new BigDecimal("45000.00"));
	}

	public static CreateEventExpenseRequest expense(UUID paidByParticipantId, BigDecimal amount) {
		return new CreateEventExpenseRequest(
			"Dinner",
			"Saturday dinner",
			amount,
			paidByParticipantId
		);
	}

	public static CreateEventExpenseRequest invalidAmountExpense(UUID paidByParticipantId) {
		return new CreateEventExpenseRequest(
			"Dinner",
			"Saturday dinner",
			BigDecimal.ZERO,
			paidByParticipantId
		);
	}

	public static UpdateEventExpenseRequest updateExpense(UUID paidByParticipantId) {
		return new UpdateEventExpenseRequest(
			"Updated dinner",
			"Saturday night dinner",
			new BigDecimal("50000.00"),
			paidByParticipantId
		);
	}

	private static String uniqueToken(String prefix) {
		return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
	}
}
