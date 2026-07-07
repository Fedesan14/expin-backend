package com.fedesan14.expin_backend.events.controller.implementations;

import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;
import com.fedesan14.expin_backend.events.controller.interfaces.EventSettlementController;
import com.fedesan14.expin_backend.events.controller.requests.CalculateEventSettlementRequest;
import com.fedesan14.expin_backend.events.controller.responses.EventSettlementResponse;
import com.fedesan14.expin_backend.events.service.interfaces.EventSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventSettlementControllerImpl implements EventSettlementController {

	private final EventSettlementService eventSettlementService;

	@Override
	public EventSettlementResponse calculate(User currentUser, UUID eventId, CalculateEventSettlementRequest request) {
		return EventSettlementResponse.from(eventSettlementService.calculate(currentUser, eventId, request.strategy()));
	}
}
