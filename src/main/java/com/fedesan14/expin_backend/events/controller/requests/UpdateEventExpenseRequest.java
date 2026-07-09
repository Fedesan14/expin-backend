package com.fedesan14.expin_backend.events.controller.requests;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEventExpenseRequest(
	@NotBlank
	@Size(max = 255)
	String title,

	@Size(max = 1000)
	String description,

	@NotNull
	@DecimalMin(value = "0.00", inclusive = false)
	BigDecimal amount,

	@NotNull
	UUID paidByParticipantId,

	@NotEmpty
	Set<@NotNull UUID> owedByParticipantIds
) {
}
