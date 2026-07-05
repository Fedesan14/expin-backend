package com.fedesan14.expin_backend.auth.security.jwt;

import java.time.Instant;

public record JwtTokenPair(
	String sessionToken,
	Instant sessionTokenExpiresAt,
	String refreshToken,
	Instant refreshTokenExpiresAt
) {
}
