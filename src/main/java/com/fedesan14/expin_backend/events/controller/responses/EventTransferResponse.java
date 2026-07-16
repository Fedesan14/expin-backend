package com.fedesan14.expin_backend.events.controller.responses;

import java.math.BigDecimal;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.model.EventTransfer;

public record EventTransferResponse(
	UUID fromParticipantId,
	String fromDisplayName,
	UUID toParticipantId,
	String toDisplayName,
	BigDecimal amount
) {

	public static EventTransferResponse from(EventTransfer transfer) {
		return new EventTransferResponse(
			transfer.getFromParticipantId(),
			transfer.getFromDisplayName(),
			transfer.getToParticipantId(),
			transfer.getToDisplayName(),
			transfer.getAmount()
		);
	}
}
