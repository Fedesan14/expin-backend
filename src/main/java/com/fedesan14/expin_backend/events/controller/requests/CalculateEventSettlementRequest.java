package com.fedesan14.expin_backend.events.controller.requests;

import com.fedesan14.expin_backend.events.components.settlement_calculator.enums.EventSettlementStrategy;
import jakarta.validation.constraints.NotNull;

public record CalculateEventSettlementRequest(
	@NotNull
	EventSettlementStrategy strategy
) {
}
