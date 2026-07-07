package com.fedesan14.expin_backend.events.controller.implementations;

import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;
import com.fedesan14.expin_backend.events.controller.interfaces.EventExpenseController;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.responses.EventExpenseResponse;
import com.fedesan14.expin_backend.events.service.interfaces.EventExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventExpenseControllerImpl implements EventExpenseController {

	private final EventExpenseService eventExpenseService;

	@Override
	public EventExpenseResponse create(User currentUser, UUID eventId, CreateEventExpenseRequest request) {
		return EventExpenseResponse.from(eventExpenseService.create(currentUser, eventId, request));
	}

	@Override
	public EventExpenseResponse findById(User currentUser, UUID eventId, UUID expenseId) {
		return EventExpenseResponse.from(eventExpenseService.findById(currentUser, eventId, expenseId));
	}

	@Override
	public EventExpenseResponse update(User currentUser, UUID eventId, UUID expenseId, UpdateEventExpenseRequest request) {
		return EventExpenseResponse.from(eventExpenseService.update(currentUser, eventId, expenseId, request));
	}

	@Override
	public void delete(User currentUser, UUID eventId, UUID expenseId) {
		eventExpenseService.delete(currentUser, eventId, expenseId);
	}
}
