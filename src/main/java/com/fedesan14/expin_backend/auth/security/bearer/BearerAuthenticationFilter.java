package com.fedesan14.expin_backend.auth.security.bearer;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class BearerAuthenticationFilter extends OncePerRequestFilter {

	private final BearerAuthenticationProvider bearerAuthenticationProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String token = authorization.substring("Bearer ".length()).trim();
			Authentication authentication = bearerAuthenticationProvider.authenticate(new BearerAuthenticationToken(token));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} catch (AuthenticationException exception) {
			SecurityContextHolder.clearContext();
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
		}
	}
}
