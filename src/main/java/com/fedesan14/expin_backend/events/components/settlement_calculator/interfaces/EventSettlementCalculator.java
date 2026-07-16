package com.fedesan14.expin_backend.events.components.settlement_calculator.interfaces;

import com.fedesan14.expin_backend.events.data.model.Event;
import com.fedesan14.expin_backend.events.data.model.EventSettlement;
import com.fedesan14.expin_backend.events.components.settlement_calculator.enums.EventSettlementStrategy;

public interface EventSettlementCalculator {

	EventSettlementStrategy strategy();

	EventSettlement calculate(Event event);
}
