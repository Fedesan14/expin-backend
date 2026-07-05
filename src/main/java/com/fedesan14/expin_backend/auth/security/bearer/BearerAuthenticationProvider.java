package com.fedesan14.expin_backend.auth.security.bearer;

import com.fedesan14.expin_backend.auth.data.repository.UserRepository;
import com.fedesan14.expin_backend.auth.security.jwt.JwtClaims;
import com.fedesan14.expin_backend.auth.security.jwt.JwtService;
import com.fedesan14.expin_backend.auth.security.jwt.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BearerAuthenticationProvider implements AuthenticationProvider {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	@Override
	public Authentication authenticate(Authentication authentication) {
		if (!(authentication instanceof BearerAuthenticationToken bearerAuthentication)) {
			return null;
		}

		String token = (String) bearerAuthentication.getCredentials();
		JwtClaims claims = jwtService.validate(token, TokenType.SESSION);

		return userRepository.findById(claims.userId())
			.map(user -> new BearerAuthenticationToken(user, token, user.getAuthorities()))
			.orElseThrow(() -> new BadCredentialsException("Invalid token"));
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return BearerAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
