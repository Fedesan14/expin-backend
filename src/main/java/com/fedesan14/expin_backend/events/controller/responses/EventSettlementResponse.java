package com.fedesan14.expin_backend.events.controller.responses;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.model.EventSettlement;
import com.fedesan14.expin_backend.events.data.model.EventSettlementStrategy;

public record EventSettlementResponse(
	UUID eventId,
	EventSettlementStrategy strategy,
	BigDecimal totalAmount,
	int participantCount,
	List<EventParticipantBalanceResponse> balances,
	List<EventTransferResponse> transfers
) {

	public static EventSettlementResponse from(EventSettlement settlement) {
		return new EventSettlementResponse(
			settlement.eventId(),
			settlement.strategy(),
			settlement.totalAmount(),
			settlement.participantCount(),
			settlement.balances().stream()
				.map(EventParticipantBalanceResponse::from)
				.toList(),
			settlement.transfers().stream()
				.map(EventTransferResponse::from)
				.toList()
		);
	}
}
