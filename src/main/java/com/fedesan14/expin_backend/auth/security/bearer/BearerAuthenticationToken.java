package com.fedesan14.expin_backend.auth.security.bearer;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class BearerAuthenticationToken extends AbstractAuthenticationToken {

	private final String token;
	private final Object principal;

	public BearerAuthenticationToken(String token) {
		super(Collections.emptyList());
		this.token = token;
		this.principal = null;
		setAuthenticated(false);
	}

	public BearerAuthenticationToken(Object principal, String token, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.token = token;
		this.principal = principal;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return token;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}
}
