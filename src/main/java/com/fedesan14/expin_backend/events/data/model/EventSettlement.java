package com.fedesan14.expin_backend.events.data.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EventSettlement(
	UUID eventId,
	EventSettlementStrategy strategy,
	BigDecimal totalAmount,
	int participantCount,
	List<EventParticipantBalance> balances,
	List<EventTransfer> transfers
) {
}
