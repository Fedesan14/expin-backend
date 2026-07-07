package com.fedesan14.expin_backend.events.controller.interfaces;

import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;
import com.fedesan14.expin_backend.events.controller.requests.CalculateEventSettlementRequest;
import com.fedesan14.expin_backend.events.controller.responses.EventSettlementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/events/{eventId}/settlement_calculate")
public interface EventSettlementController {

	@Operation(summary = "Calculate the settlement for a shared event")
	@ApiResponse(
		responseCode = "200",
		description = "Event settlement calculated",
		content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventSettlementResponse.class))
	)
	@ApiResponse(responseCode = "400", description = "Unsupported settlement strategy", content = @Content)
	@ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token", content = @Content)
	@ApiResponse(responseCode = "403", description = "User does not participate in this event", content = @Content)
	@ApiResponse(responseCode = "404", description = "Event not found", content = @Content)
	@PostMapping
	EventSettlementResponse calculate(
		@Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
		@PathVariable UUID eventId,
		@Valid @RequestBody CalculateEventSettlementRequest request
	);
}
