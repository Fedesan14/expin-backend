package com.fedesan14.expin_backend.events.data.model;

import java.math.BigDecimal;
import java.util.UUID;

public record EventTransfer(
	UUID fromParticipantId,
	String fromDisplayName,
	UUID toParticipantId,
	String toDisplayName,
	BigDecimal amount
) {
}
