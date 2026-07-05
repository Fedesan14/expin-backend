package com.fedesan14.expin_backend.auth;

import java.util.UUID;
import java.util.stream.Stream;

import com.fedesan14.expin_backend.auth.controller.requests.SignUpRequest;
import org.junit.jupiter.params.provider.Arguments;

public final class AuthDataMock {

	public static final String VALID_PASSWORD = "password123";

	private AuthDataMock() {
	}

	public static SignUpRequest validSignUpRequest() {
		return signUpRequest("user");
	}

	public static SignUpRequest privateEndpointSignUpRequest() {
		return signUpRequest("private");
	}

	public static Stream<Arguments> duplicateSignupCases() {
		SignUpRequest existingUserForUsernameCase = signUpRequest("duplicated-username");
		SignUpRequest existingUserForEmailCase = signUpRequest("duplicated-email");

		return Stream.of(
			Arguments.of(
				"username",
				existingUserForUsernameCase,
				new SignUpRequest(
					existingUserForUsernameCase.username(),
					VALID_PASSWORD,
					uniqueEmail("second")
				)
			),
			Arguments.of(
				"email",
				existingUserForEmailCase,
				new SignUpRequest(
					uniqueUsername("second"),
					VALID_PASSWORD,
					existingUserForEmailCase.email()
				)
			)
		);
	}

	public static Stream<Arguments> loginIdentifierCases() {
		SignUpRequest usernameLoginUser = signUpRequest("login-username");
		SignUpRequest emailLoginUser = signUpRequest("login-email");

		return Stream.of(
			Arguments.of("username", usernameLoginUser, usernameLoginUser.username()),
			Arguments.of("email", emailLoginUser, emailLoginUser.email())
		);
	}

	private static SignUpRequest signUpRequest(String prefix) {
		return new SignUpRequest(uniqueUsername(prefix), VALID_PASSWORD, uniqueEmail(prefix));
	}

	private static String uniqueUsername(String prefix) {
		return prefix + "-" + uniqueToken();
	}

	private static String uniqueEmail(String prefix) {
		return prefix + "-" + uniqueToken() + "@example.com";
	}

	private static String uniqueToken() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

}
