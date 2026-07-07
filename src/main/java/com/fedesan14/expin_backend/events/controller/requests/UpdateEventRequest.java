package com.fedesan14.expin_backend.events.controller.requests;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateEventRequest(
	@NotBlank
	@Size(max = 255)
	String title,

	@Size(max = 1000)
	String description,

	LocalDate startDate,

	LocalDate endDate,

	@Valid
	List<EventParticipantRequest> participants
) {

	@AssertTrue(message = "endDate must not be before startDate")
	public boolean hasValidDateRange() {
		return startDate == null || endDate == null || !endDate.isBefore(startDate);
	}
}
