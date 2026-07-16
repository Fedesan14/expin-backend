package com.fedesan14.expin_backend.events.service.interfaces;

import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.events.data.model.EventSettlement;
import com.fedesan14.expin_backend.events.components.settlement_calculator.enums.EventSettlementStrategy;

public interface EventSettlementService {

	EventSettlement calculate(User currentUser, UUID eventId, EventSettlementStrategy strategy);
}
