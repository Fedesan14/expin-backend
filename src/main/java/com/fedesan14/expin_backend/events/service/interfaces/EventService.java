package com.fedesan14.expin_backend.events.service.interfaces;

import java.util.List;
import java.util.UUID;

import com.fedesan14.expin_backend.events.controller.responses.EventResponse;
import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventRequest;
import com.fedesan14.expin_backend.events.data.model.Event;

public interface EventService {

	Event create(User currentUser, CreateEventRequest request);

	List<Event> findCurrentUserEvents(User currentUser);

	Event findById(User currentUser, UUID eventId);

	Event joinByInviteToken(User currentUser, String inviteToken);

	Event update(User currentUser, UUID eventId, UpdateEventRequest request);

	void delete(User currentUser, UUID eventId);

    Event saveEvent(Event event);

    EventResponse closeEvent(User currentUser, UUID eventId);
}
