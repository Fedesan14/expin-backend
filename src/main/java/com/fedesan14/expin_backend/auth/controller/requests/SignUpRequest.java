package com.fedesan14.expin_backend.auth.controller.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
	@NotBlank
	@Size(max = 30)
	String username,

	@NotBlank
	@Size(min = 8, max = 72)
	String password,

	@NotBlank
	@Email
	@Size(max = 255)
	String email
) {
}
