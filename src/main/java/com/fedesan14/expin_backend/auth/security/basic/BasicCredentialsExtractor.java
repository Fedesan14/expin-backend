package com.fedesan14.expin_backend.auth.security.basic;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class BasicCredentialsExtractor {

	public BasicCredentials extract(HttpHeaders headers) {
		String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith("Basic ")) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Basic credentials");
		}

		String decoded = decode(authorization.substring("Basic ".length()));
		int separatorIndex = decoded.indexOf(':');
		if (separatorIndex < 1) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Basic credentials");
		}

		String identifier = decoded.substring(0, separatorIndex);
		String password = decoded.substring(separatorIndex + 1);
		if (password.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Basic credentials");
		}

		return new BasicCredentials(identifier, password);
	}

	private String decode(String encodedCredentials) {
		try {
			byte[] decoded = Base64.getDecoder().decode(encodedCredentials);
			return new String(decoded, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException exception) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Basic credentials");
		}
	}
}
