package com.fedesan14.expin_backend.events.service.implementations;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventExpenseRequest;
import com.fedesan14.expin_backend.events.data.model.Event;
import com.fedesan14.expin_backend.events.data.model.EventExpense;
import com.fedesan14.expin_backend.events.data.model.EventParticipant;
import com.fedesan14.expin_backend.events.data.repository.EventExpenseRepository;
import com.fedesan14.expin_backend.events.service.interfaces.EventExpenseService;
import com.fedesan14.expin_backend.events.service.interfaces.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EventExpenseServiceImpl implements EventExpenseService {

	private final EventService eventService;
	private final EventExpenseRepository eventExpenseRepository;

	@Override
	@Transactional
	public EventExpense create(User currentUser, UUID eventId, CreateEventExpenseRequest request) {
		Event event = eventService.findById(currentUser, eventId);
		ensurePositiveAmount(request.amount());

		EventParticipant payer = findParticipant(event, request.paidByParticipantId());
		Set<EventParticipant> owedByParticipants = findParticipants(event, request.owedByParticipantIds());
		EventExpense expense = new EventExpense(
			request.title(),
			request.description(),
			request.amount(),
			payer,
			owedByParticipants
		);
		event.addExpense(expense);
		eventService.saveEvent(event);

		return expense;
	}

	@Override
	@Transactional(readOnly = true)
	public EventExpense findById(User currentUser, UUID eventId, UUID expenseId) {
		eventService.findById(currentUser, eventId);
		EventExpense expense = findExpense(eventId, expenseId);
		return expense;
	}

	@Override
	@Transactional
	public EventExpense update(User currentUser, UUID eventId, UUID expenseId, UpdateEventExpenseRequest request) {
		Event event = eventService.findById(currentUser, eventId);
		EventExpense expense = findExpense(eventId, expenseId);
		ensurePositiveAmount(request.amount());

		expense.updateDetails(
			request.title(),
			request.description(),
			request.amount(),
			findParticipant(event, request.paidByParticipantId()),
			findParticipants(event, request.owedByParticipantIds())
		);

		return eventExpenseRepository.save(expense);
	}

	@Override
	@Transactional
	public void delete(User currentUser, UUID eventId, UUID expenseId) {
		Event event = eventService.findById(currentUser, eventId);
		EventExpense expense = findExpense(eventId, expenseId);
		event.removeExpense(expense);
	}

	private EventExpense findExpense(UUID eventId, UUID expenseId) {
		return eventExpenseRepository.findByIdAndEventId(expenseId, eventId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
	}

	private EventParticipant findParticipant(Event event, UUID participantId) {
		return event.findParticipant(participantId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
	}

	private Set<EventParticipant> findParticipants(Event event, Set<UUID> participantIds) {
		Set<EventParticipant> participants = new LinkedHashSet<>();
		for (UUID participantId : participantIds) {
			participants.add(findParticipant(event, participantId));
		}
		return participants;
	}

	private void ensurePositiveAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense amount must be greater than zero");
		}
	}
}
