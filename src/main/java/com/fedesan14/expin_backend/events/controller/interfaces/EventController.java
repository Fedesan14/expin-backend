package com.fedesan14.expin_backend.events.controller.interfaces;

import java.util.List;
import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventRequest;
import com.fedesan14.expin_backend.events.controller.responses.EventResponse;
import com.fedesan14.expin_backend.events.controller.responses.EventSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/events")
public interface EventController {

	@Operation(summary = "Create a shared event")
	@ApiResponse(
		responseCode = "201",
		description = "Event created",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventResponse.class))
	)
	@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	EventResponse create(
		@Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
		@Valid @RequestBody CreateEventRequest request
	);

	@Operation(summary = "List events where the authenticated user participates")
	@ApiResponse(
		responseCode = "200",
		description = "Visible event summaries",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON_VALUE,
			array = @ArraySchema(schema = @Schema(implementation = EventSummaryResponse.class))
		)
	)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@GetMapping
	List<EventSummaryResponse> findCurrentUserEvents(@Parameter(hidden = true) @AuthenticationPrincipal User currentUser);

	@Operation(summary = "Join a shared event using an invite link token")
	@ApiResponse(
		responseCode = "200",
		description = "Authenticated user joined the event",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventResponse.class))
	)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "404", description = "Event invite not found", content = @Content)
	@PostMapping("/invite/{inviteToken}")
	EventResponse joinByInviteToken(
		@Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
		@PathVariable String inviteToken
	);

	@Operation(summary = "Get a shared event with participants and expenses")
	@ApiResponse(
		responseCode = "200",
		description = "Event detail",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventResponse.class))
	)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "403", description = "User cannot access this event", content = @Content)
	@ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
	@GetMapping("/{eventId}")
	EventResponse findById(@Parameter(hidden = true) @AuthenticationPrincipal User currentUser, @PathVariable UUID eventId);

	@Operation(summary = "Update a shared event")
	@ApiResponse(
		responseCode = "200",
		description = "Event updated",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventResponse.class))
	)
	@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "403", description = "Only the owner can modify this event", content = @Content)
	@ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
	@ApiResponse(responseCode = "409", description = "Participant cannot be removed", content = @Content)
	@PutMapping("/{eventId}")
	EventResponse update(
		@Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
		@PathVariable UUID eventId,
		@Valid @RequestBody UpdateEventRequest request
	);

	@Operation(summary = "Delete a shared event")
	@ApiResponse(responseCode = "204", description = "Event deleted", content = @Content)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "403", description = "Only the owner can delete this event", content = @Content)
	@ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
	@DeleteMapping("/{eventId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(@Parameter(hidden = true) @AuthenticationPrincipal User currentUser, @PathVariable UUID eventId);

    @PostMapping("/{eventId}/close_event")
    EventResponse closeEvent(@Parameter(hidden = true) @AuthenticationPrincipal User currentUser, @PathVariable UUID eventId);
}
