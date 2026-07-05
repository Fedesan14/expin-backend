package com.fedesan14.expin_backend.auth.controller.responses;

import java.time.Instant;

public record AuthTokensResponse(
	String sessionToken,
	Instant sessionTokenExpiresAt,
	String refreshToken,
	Instant refreshTokenExpiresAt
) {
}
