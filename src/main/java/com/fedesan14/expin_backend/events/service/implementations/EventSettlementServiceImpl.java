package com.fedesan14.expin_backend.events.service.implementations;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;
import com.fedesan14.expin_backend.events.components.settlement_calculator.interfaces.EventSettlementCalculator;
import com.fedesan14.expin_backend.events.data.model.Event;
import com.fedesan14.expin_backend.events.data.model.EventSettlement;
import com.fedesan14.expin_backend.events.data.model.EventSettlementStrategy;
import com.fedesan14.expin_backend.events.service.interfaces.EventSettlementService;
import com.fedesan14.expin_backend.events.service.interfaces.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventSettlementServiceImpl implements EventSettlementService {

	private final EventService eventService;
	private final Map<EventSettlementStrategy, EventSettlementCalculator> calculators;

	public EventSettlementServiceImpl(EventService eventService, List<EventSettlementCalculator> calculators) {
		this.eventService = eventService;
		this.calculators = new EnumMap<>(EventSettlementStrategy.class);
		for (EventSettlementCalculator calculator : calculators) {
			this.calculators.put(calculator.strategy(), calculator);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public EventSettlement calculate(User currentUser, UUID eventId, EventSettlementStrategy strategy) {
		Event event = eventService.findById(currentUser, eventId);

		if (strategy == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Settlement strategy is required");
		}

		EventSettlementCalculator calculator = calculators.get(strategy);
		if (calculator == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported settlement strategy");
		}

		return calculator.calculate(event);
	}
}
