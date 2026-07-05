package com.fedesan14.expin_backend.auth.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

	private String secret;
	private long sessionExpirationMinutes = 15;
	private long refreshExpirationMinutes = 30;

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public long getSessionExpirationMinutes() {
		return sessionExpirationMinutes;
	}

	public void setSessionExpirationMinutes(long sessionExpirationMinutes) {
		this.sessionExpirationMinutes = sessionExpirationMinutes;
	}

	public long getRefreshExpirationMinutes() {
		return refreshExpirationMinutes;
	}

	public void setRefreshExpirationMinutes(long refreshExpirationMinutes) {
		this.refreshExpirationMinutes = refreshExpirationMinutes;
	}
}
