package com.fedesan14.expin_backend.events.controller.responses;

import java.math.BigDecimal;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.model.EventParticipantBalance;

public record EventParticipantBalanceResponse(
	UUID participantId,
	String displayName,
	BigDecimal paidAmount,
	BigDecimal owedAmount,
	BigDecimal balance
) {

	public static EventParticipantBalanceResponse from(EventParticipantBalance balance) {
		return new EventParticipantBalanceResponse(
			balance.getParticipantId(),
			balance.getDisplayName(),
			balance.getPaidAmount(),
			balance.getOwedAmount(),
			balance.getBalance()
		);
	}
}
