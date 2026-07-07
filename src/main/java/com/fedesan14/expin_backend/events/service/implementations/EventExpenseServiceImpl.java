package com.fedesan14.expin_backend.events.service.implementations;

import java.math.BigDecimal;
import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventExpenseRequest;
import com.fedesan14.expin_backend.events.data.model.Event;
import com.fedesan14.expin_backend.events.data.model.EventExpense;
import com.fedesan14.expin_backend.events.data.model.EventParticipant;
import com.fedesan14.expin_backend.events.data.repository.EventExpenseRepository;
import com.fedesan14.expin_backend.events.data.repository.EventRepository;
import com.fedesan14.expin_backend.events.service.interfaces.EventExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EventExpenseServiceImpl implements EventExpenseService {

	private final EventRepository eventRepository;
	private final EventExpenseRepository eventExpenseRepository;

	@Override
	@Transactional
	public EventExpense create(User currentUser, UUID eventId, CreateEventExpenseRequest request) {
		Event event = findEvent(eventId);
		ensureUserParticipates(event, currentUser);
		ensurePositiveAmount(request.amount());

		EventParticipant payer = findParticipant(event, request.paidByParticipantId());
		EventExpense expense = new EventExpense(
			request.title(),
			request.description(),
			request.amount(),
			payer
		);
		event.addExpense(expense);

		return eventRepository.save(event)
			.getExpenses()
			.stream()
			.filter(savedExpense -> savedExpense.getId().equals(expense.getId()))
			.findFirst()
			.orElse(expense);
	}

	@Override
	@Transactional(readOnly = true)
	public EventExpense findById(User currentUser, UUID eventId, UUID expenseId) {
		EventExpense expense = findExpense(eventId, expenseId);
		ensureUserParticipates(expense.getEvent(), currentUser);
		return expense;
	}

	@Override
	@Transactional
	public EventExpense update(User currentUser, UUID eventId, UUID expenseId, UpdateEventExpenseRequest request) {
		EventExpense expense = findExpense(eventId, expenseId);
		Event event = expense.getEvent();
		ensureUserParticipates(event, currentUser);
		ensurePositiveAmount(request.amount());

		expense.updateDetails(
			request.title(),
			request.description(),
			request.amount(),
			findParticipant(event, request.paidByParticipantId())
		);

		return eventExpenseRepository.save(expense);
	}

	@Override
	@Transactional
	public void delete(User currentUser, UUID eventId, UUID expenseId) {
		EventExpense expense = findExpense(eventId, expenseId);
		Event event = expense.getEvent();
		ensureUserParticipates(event, currentUser);
		event.removeExpense(expense);
		eventRepository.save(event);
	}

	private Event findEvent(UUID eventId) {
		return eventRepository.findWithDetailsById(eventId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
	}

	private EventExpense findExpense(UUID eventId, UUID expenseId) {
		return eventExpenseRepository.findByIdAndEventId(expenseId, eventId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
	}

	private void ensureUserParticipates(Event event, User user) {
		if (!event.hasUserParticipant(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not participate in this event");
		}
	}

	private EventParticipant findParticipant(Event event, UUID participantId) {
		return event.findParticipant(participantId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
	}

	private void ensurePositiveAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense amount must be greater than zero");
		}
	}
}
