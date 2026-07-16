package com.fedesan14.expin_backend.events.controller.implementations;

import java.util.List;
import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.events.controller.interfaces.EventController;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventRequest;
import com.fedesan14.expin_backend.events.controller.responses.EventResponse;
import com.fedesan14.expin_backend.events.controller.responses.EventSummaryResponse;
import com.fedesan14.expin_backend.events.service.interfaces.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventControllerImpl implements EventController {

	private final EventService eventService;

	@Override
	public EventResponse create(User currentUser, CreateEventRequest request) {
		return EventResponse.from(eventService.create(currentUser, request));
	}

	@Override
	public List<EventSummaryResponse> findCurrentUserEvents(User currentUser) {
		return eventService.findCurrentUserEvents(currentUser).stream()
			.map(EventSummaryResponse::from)
			.toList();
	}

	@Override
	public EventResponse findById(User currentUser, UUID eventId) {
		return EventResponse.from(eventService.findById(currentUser, eventId));
	}

	@Override
	public EventResponse joinByInviteToken(User currentUser, String inviteToken) {
		return EventResponse.from(eventService.joinByInviteToken(currentUser, inviteToken));
	}

	@Override
	public EventResponse update(User currentUser, UUID eventId, UpdateEventRequest request) {
		return EventResponse.from(eventService.update(currentUser, eventId, request));
	}

	@Override
	public void delete(User currentUser, UUID eventId) {
		eventService.delete(currentUser, eventId);
	}

    @Override
    public EventResponse closeEvent(User currentUser, UUID eventId) {
        return eventService.closeEvent(currentUser, eventId);
    }

}
