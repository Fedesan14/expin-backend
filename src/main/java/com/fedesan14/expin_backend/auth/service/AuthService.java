package com.fedesan14.expin_backend.auth.service;

import com.fedesan14.expin_backend.auth.controller.requests.SignUpRequest;
import com.fedesan14.expin_backend.auth.controller.responses.AuthTokensResponse;
import com.fedesan14.expin_backend.users.data.model.User;
import com.fedesan14.expin_backend.auth.security.basic.BasicCredentials;

public interface AuthService {

	User signup(SignUpRequest request);

	AuthTokensResponse login(BasicCredentials credentials, String autologinHash);

    AuthTokensResponse autologin(String autologinHash, String username);
}
