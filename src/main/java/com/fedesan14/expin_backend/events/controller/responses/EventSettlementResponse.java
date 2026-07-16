package com.fedesan14.expin_backend.events.controller.responses;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.model.EventSettlement;
import com.fedesan14.expin_backend.events.components.settlement_calculator.enums.EventSettlementStrategy;

public record EventSettlementResponse(
	EventSettlementStrategy strategy,
	BigDecimal totalAmount,
	int participantCount,
	List<EventParticipantBalanceResponse> balances,
	List<EventTransferResponse> transfers
) {

	public static EventSettlementResponse from(EventSettlement settlement) {
        if (settlement == null) return null;
		return new EventSettlementResponse(
			settlement.getStrategy(),
			settlement.getTotalAmount(),
			settlement.getParticipantCount(),
			settlement.getBalances().stream()
				.map(EventParticipantBalanceResponse::from)
				.toList(),
			settlement.getTransfers().stream()
				.map(EventTransferResponse::from)
				.toList()
		);
	}
}
