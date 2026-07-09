package com.fedesan14.expin_backend.auth.controller.implementations;

import com.fedesan14.expin_backend.auth.controller.interfaces.AuthController;
import com.fedesan14.expin_backend.auth.controller.requests.SignUpRequest;
import com.fedesan14.expin_backend.auth.controller.responses.AuthTokensResponse;
import com.fedesan14.expin_backend.auth.controller.responses.UserResponse;
import com.fedesan14.expin_backend.auth.security.basic.BasicCredentials;
import com.fedesan14.expin_backend.auth.security.basic.BasicCredentialsExtractor;
import com.fedesan14.expin_backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

	private final AuthService authService;
	private final BasicCredentialsExtractor basicCredentialsExtractor;

	@Override
	public UserResponse signup(SignUpRequest request) {
		return UserResponse.from(authService.signup(request));
	}

	@Override
	public AuthTokensResponse login(HttpHeaders headers, String autologinHash) {
		BasicCredentials credentials = basicCredentialsExtractor.extract(headers);
		return authService.login(credentials, autologinHash);
	}

    @Override
    public AuthTokensResponse autologin(String autologinHash, String username) {
        return authService.autologin(autologinHash, username);
    }
}
