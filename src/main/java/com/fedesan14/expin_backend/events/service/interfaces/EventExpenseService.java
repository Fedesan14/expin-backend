package com.fedesan14.expin_backend.events.service.interfaces;

import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventExpenseRequest;
import com.fedesan14.expin_backend.events.data.model.EventExpense;

public interface EventExpenseService {

	EventExpense create(User currentUser, UUID eventId, CreateEventExpenseRequest request);

	EventExpense findById(User currentUser, UUID eventId, UUID expenseId);

	EventExpense update(User currentUser, UUID eventId, UUID expenseId, UpdateEventExpenseRequest request);

	void delete(User currentUser, UUID eventId, UUID expenseId);
}
