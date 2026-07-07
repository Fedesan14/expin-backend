package com.fedesan14.expin_backend.events.controller.requests;

import com.fedesan14.expin_backend.events.data.model.EventSettlementStrategy;
import jakarta.validation.constraints.NotNull;

public record CalculateEventSettlementRequest(
	@NotNull
	EventSettlementStrategy strategy
) {
}
