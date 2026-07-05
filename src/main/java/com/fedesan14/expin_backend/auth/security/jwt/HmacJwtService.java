package com.fedesan14.expin_backend.auth.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fedesan14.expin_backend.auth.data.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class HmacJwtService implements JwtService {

	private static final String HMAC_ALGORITHM = "HmacSHA256";
	private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

	private final JwtProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public JwtTokenPair createTokenPair(User user) {
		Instant now = Clock.systemUTC().instant();
		Instant sessionExpiresAt = now.plus(Duration.ofMinutes(properties.getSessionExpirationMinutes()));
		Instant refreshExpiresAt = now.plus(Duration.ofMinutes(properties.getRefreshExpirationMinutes()));

		return new JwtTokenPair(
			createToken(user, TokenType.SESSION, now, sessionExpiresAt),
			sessionExpiresAt,
			createToken(user, TokenType.REFRESH, now, refreshExpiresAt),
			refreshExpiresAt
		);
	}

	@Override
	public JwtClaims validate(String token, TokenType expectedTokenType) {
		String[] parts = token.split("\\.");
		if (parts.length != 3) {
			throw invalidToken();
		}

		String signedContent = parts[0] + "." + parts[1];
		if (!MessageDigest.isEqual(sign(signedContent).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
			throw invalidToken();
		}

		try {
			Map<String, Object> payload = readPayload(parts[1]);
			TokenType tokenType = TokenType.valueOf(requiredString(payload, "typ"));
			if (tokenType != expectedTokenType) {
				throw invalidToken();
			}

			Instant expiresAt = Instant.ofEpochSecond(requiredLong(payload, "exp"));
			if (!expiresAt.isAfter(Clock.systemUTC().instant())) {
				throw invalidToken();
			}

			return new JwtClaims(
				UUID.fromString(requiredString(payload, "sub")),
				requiredString(payload, "username"),
				tokenType,
				expiresAt
			);
		} catch (IllegalArgumentException exception) {
			throw invalidToken();
		}
	}

	private String createToken(User user, TokenType tokenType, Instant issuedAt, Instant expiresAt) {
		Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
		Map<String, Object> payload = Map.of(
			"sub", user.getId().toString(),
			"username", user.getUsername(),
			"typ", tokenType.name(),
			"iat", issuedAt.getEpochSecond(),
			"exp", expiresAt.getEpochSecond()
		);
		String signedContent = encode(header) + "." + encode(payload);
		return signedContent + "." + sign(signedContent);
	}

	private String encode(Map<String, Object> value) {
		try {
			return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
		} catch (Exception exception) {
			throw new IllegalStateException("Could not encode JWT", exception);
		}
	}

	private Map<String, Object> readPayload(String encodedPayload) {
		try {
			return objectMapper.readValue(DECODER.decode(encodedPayload), Map.class);
		} catch (Exception exception) {
			throw invalidToken();
		}
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_ALGORITHM);
			mac.init(new SecretKeySpec(properties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
			return ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Could not sign JWT", exception);
		}
	}

	private String requiredString(Map<String, Object> payload, String claim) {
		Object value = payload.get(claim);
		if (value instanceof String stringValue && !stringValue.isBlank()) {
			return stringValue;
		}
		throw invalidToken();
	}

	private long requiredLong(Map<String, Object> payload, String claim) {
		Object value = payload.get(claim);
		if (value instanceof Number numberValue) {
			return numberValue.longValue();
		}
		throw invalidToken();
	}

	private BadCredentialsException invalidToken() {
		return new BadCredentialsException("Invalid token");
	}
}
