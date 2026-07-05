package com.fedesan14.expin_backend.auth.security.jwt;

import com.fedesan14.expin_backend.auth.data.model.User;

public interface JwtService {

	JwtTokenPair createTokenPair(User user);

	JwtClaims validate(String token, TokenType expectedTokenType);
}
