package com.fedesan14.expin_backend.events.controller.responses;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.model.EventExpense;

public record EventExpenseResponse(
	UUID id,
	String title,
	String description,
	BigDecimal amount,
	UUID paidByParticipantId,
	List<UUID> owedByParticipantIds
) {

	public static EventExpenseResponse from(EventExpense expense) {
		return new EventExpenseResponse(
			expense.getId(),
			expense.getTitle(),
			expense.getDescription(),
			expense.getAmount(),
			expense.getPaidByParticipant().getId(),
			expense.getOwedByParticipants().stream()
				.map(participant -> participant.getId())
				.sorted(Comparator.comparing(UUID::toString))
				.toList()
		);
	}
}
