package com.fedesan14.expin_backend.auth.security.jwt;

import java.time.Instant;
import java.util.UUID;

public record JwtClaims(UUID userId, String username, TokenType tokenType, Instant expiresAt) {
}
