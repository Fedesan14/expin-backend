package com.fedesan14.expin_backend.events.data.model;

import java.math.BigDecimal;
import java.util.UUID;

public record EventParticipantBalance(
	UUID participantId,
	String displayName,
	BigDecimal paidAmount,
	BigDecimal owedAmount,
	BigDecimal balance
) {
}
