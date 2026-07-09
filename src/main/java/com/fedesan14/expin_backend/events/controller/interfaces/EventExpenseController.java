package com.fedesan14.expin_backend.events.controller.interfaces;

import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.responses.EventExpenseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/events/{eventId}/expenses")
public interface EventExpenseController {

	@Operation(summary = "Create an expense inside a shared event")
	@ApiResponse(
		responseCode = "201",
		description = "Expense created",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventExpenseResponse.class))
	)
	@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "403", description = "User does not participate in this event", content = @Content)
	@ApiResponse(responseCode = "404", description = "Event or participant not found", content = @Content)
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	EventExpenseResponse create(
		@Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
		@PathVariable UUID eventId,
		@Valid @RequestBody CreateEventExpenseRequest request
	);

	@Operation(summary = "Get an event expense")
	@ApiResponse(
		responseCode = "200",
		description = "Expense detail",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventExpenseResponse.class))
	)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "403", description = "User does not participate in this event", content = @Content)
	@ApiResponse(responseCode = "404", description = "Expense not found", content = @Content)
	@GetMapping("/{expenseId}")
	EventExpenseResponse findById(
		@Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
		@PathVariable UUID eventId,
		@PathVariable UUID expenseId
	);

	@Operation(summary = "Update an event expense")
	@ApiResponse(
		responseCode = "200",
		description = "Expense updated",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventExpenseResponse.class))
	)
	@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "403", description = "User does not participate in this event", content = @Content)
	@ApiResponse(responseCode = "404", description = "Expense or participant not found", content = @Content)
	@PutMapping("/{expenseId}")
	EventExpenseResponse update(
		@Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
		@PathVariable UUID eventId,
		@PathVariable UUID expenseId,
		@Valid @RequestBody UpdateEventExpenseRequest request
	);

	@Operation(summary = "Delete an event expense")
	@ApiResponse(responseCode = "204", description = "Expense deleted", content = @Content)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "403", description = "User does not participate in this event", content = @Content)
	@ApiResponse(responseCode = "404", description = "Expense not found", content = @Content)
	@DeleteMapping("/{expenseId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void delete(
		@Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
		@PathVariable UUID eventId,
		@PathVariable UUID expenseId
	);
}
