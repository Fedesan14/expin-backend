package com.fedesan14.expin_backend.events.service.implementations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;
import com.fedesan14.expin_backend.auth.service.UserService;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventRequest;
import com.fedesan14.expin_backend.events.controller.requests.EventParticipantRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventRequest;
import com.fedesan14.expin_backend.events.data.model.Event;
import com.fedesan14.expin_backend.events.data.model.EventExpense;
import com.fedesan14.expin_backend.events.data.model.EventParticipant;
import com.fedesan14.expin_backend.events.data.repository.EventRepository;
import com.fedesan14.expin_backend.events.service.interfaces.EventService;
import com.fedesan14.expin_backend.events.service.interfaces.EventShareLinkGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

	private final EventRepository eventRepository;
	private final UserService userService;
	private final EventShareLinkGenerator eventShareLinkGenerator;

	@Override
	@Transactional
	public Event create(User currentUser, CreateEventRequest request) {
		Event event = new Event(
			request.title(),
			request.description(),
			request.startDate(),
			request.endDate(),
			eventShareLinkGenerator.generate(),
			currentUser
		);

		event.addParticipant(EventParticipant.user(currentUser));
		for (EventParticipant participant : requestedParticipants(request.participants())) {
			if (!participant.isUser(currentUser.getId()) && event.getParticipants().stream().noneMatch(participant::isSameIdentity)) {
				event.addParticipant(participant);
			}
		}

		return eventRepository.save(event);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Event> findCurrentUserEvents(User currentUser) {
		return eventRepository.findAllVisibleToUser(currentUser.getId());
	}

	@Override
	@Transactional(readOnly = true)
	public Event findById(User currentUser, UUID eventId) {
		Event event = findDetailedEvent(eventId);
		ensureUserCanView(event, currentUser);
		return event;
	}

	@Override
	@Transactional
	public Event update(User currentUser, UUID eventId, UpdateEventRequest request) {
		Event event = findDetailedEvent(eventId);
		ensureOwner(event, currentUser);

		event.updateDetails(request.title(), request.description(), request.startDate(), request.endDate());
		replaceParticipants(event, requestedParticipants(request.participants()));

		return eventRepository.save(event);
	}

	@Override
	@Transactional
	public void delete(User currentUser, UUID eventId) {
		Event event = findDetailedEvent(eventId);
		ensureOwner(event, currentUser);
		eventRepository.delete(event);
	}

	private Event findDetailedEvent(UUID eventId) {
		return eventRepository.findWithDetailsById(eventId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
	}

	private void ensureUserCanView(Event event, User user) {
		if (!event.isOwner(user) && !event.hasUserParticipant(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User cannot access this event");
		}
	}

	private void ensureOwner(Event event, User user) {
		if (!event.isOwner(user)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can modify this event");
		}
	}

	private void replaceParticipants(Event event, List<EventParticipant> requestedParticipants) {
		if (requestedParticipants.stream().noneMatch(participant -> participant.isUser(event.getOwner().getId()))) {
			requestedParticipants.add(EventParticipant.user(event.getOwner()));
		}

		Set<EventParticipant> participantsToKeep = new HashSet<>();
		for (EventParticipant requestedParticipant : requestedParticipants) {
			EventParticipant existingParticipant = event.getParticipants().stream()
				.filter(requestedParticipant::isSameIdentity)
				.findFirst()
				.orElse(null);

			if (existingParticipant == null) {
				event.addParticipant(requestedParticipant);
				participantsToKeep.add(requestedParticipant);
			} else {
				participantsToKeep.add(existingParticipant);
			}
		}

		List<EventParticipant> participantsToRemove = event.getParticipants().stream()
			.filter(participant -> !participantsToKeep.contains(participant))
			.toList();

		for (EventParticipant participant : participantsToRemove) {
			if (isPayer(event, participant)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot remove participant with existing expenses");
			}
			event.removeParticipant(participant);
		}
	}

	private boolean isPayer(Event event, EventParticipant participant) {
		return event.getExpenses().stream()
			.map(EventExpense::getPaidByParticipant)
			.anyMatch(payer -> payer.getId().equals(participant.getId()));
	}

	private List<EventParticipant> requestedParticipants(List<EventParticipantRequest> participantRequests) {
		if (participantRequests == null) {
			return new ArrayList<>();
		}

		return new ArrayList<>(participantRequests.stream()
			.map(this::toParticipant).toList());
	}

	private EventParticipant toParticipant(EventParticipantRequest request) {
		if (request.userId() != null) {
			return EventParticipant.user(userService.findById(request.userId()));
		}
		return EventParticipant.guest(request.guestName());
	}
}
