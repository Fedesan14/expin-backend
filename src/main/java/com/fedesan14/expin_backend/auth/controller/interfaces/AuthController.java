package com.fedesan14.expin_backend.auth.controller.interfaces;

import com.fedesan14.expin_backend.auth.controller.requests.SignUpRequest;
import com.fedesan14.expin_backend.auth.controller.responses.AuthTokensResponse;
import com.fedesan14.expin_backend.auth.controller.responses.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
public interface AuthController {

	@Operation(summary = "Register a new user")
	@ApiResponse(
		responseCode = "200",
		description = "User registered",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = UserResponse.class)
		)
	)
	@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
	@ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content)
	@PostMapping("/signup")
	UserResponse signup(@Valid @RequestBody SignUpRequest request);

	@Operation(summary = "Log in with username or email using Basic authentication")
	@ApiResponse(
		responseCode = "200",
		description = "Session and refresh JWT tokens",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = AuthTokensResponse.class)
		)
	)
	@ApiResponse(responseCode = "401", description = "Invalid Basic credentials", content = @Content)
	@PostMapping("/login")
	AuthTokensResponse login(
		@RequestHeader
		@Parameter(description = "Basic credentials", example = "Basic dXNlcm5hbWU6cGFzc3dvcmQ=")
		HttpHeaders headers
	);
}
