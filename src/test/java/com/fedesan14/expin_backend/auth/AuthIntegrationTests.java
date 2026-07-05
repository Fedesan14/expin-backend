package com.fedesan14.expin_backend.auth;

import com.fedesan14.expin_backend.auth.controller.requests.SignUpRequest;
import com.fedesan14.expin_backend.auth.controller.responses.AuthTokensResponse;
import com.fedesan14.expin_backend.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTests extends AbstractIntegrationTest {

	@Test
	void signupCreatesUser() throws Exception {
		SignUpRequest request = AuthDataMock.validSignUpRequest();

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.username").value(request.username()))
			.andExpect(jsonPath("$.profileId").isNotEmpty())
			.andExpect(jsonPath("$.email").value(request.email()))
			.andExpect(jsonPath("$.password").doesNotExist());
	}

	@ParameterizedTest(name = "rejects duplicated {0}")
	@MethodSource("com.fedesan14.expin_backend.auth.AuthDataMock#duplicateSignupCases")
	void signupRejectsDuplicatedField(
		String caseName,
		SignUpRequest existingUser,
		SignUpRequest duplicatedUser
	) throws Exception {
		signup(existingUser);

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(duplicatedUser)))
			.andExpect(status().isConflict());
	}

	@ParameterizedTest(name = "login accepts {0} and password")
	@MethodSource("com.fedesan14.expin_backend.auth.AuthDataMock#loginIdentifierCases")
	void loginAcceptsIdentifierAndPassword(
		String caseName,
		SignUpRequest user,
		String identifier
	) throws Exception {
		signup(user);

		AuthTokensResponse response = login(identifier, user.password());

		assertHasTokens(response);
	}

	@Test
	void privateEndpointRejectsMissingBearerToken() throws Exception {
		mockMvc.perform(get("/ping"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void privateEndpointAcceptsSessionToken() throws Exception {
		SignUpRequest request = AuthDataMock.privateEndpointSignUpRequest();
		signup(request);
		String sessionToken = login(request.username(), request.password()).sessionToken();

		mockMvc.perform(get("/ping")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + sessionToken))
			.andExpect(status().isOk())
			.andExpect(content().string("pong"));
	}

	private void signup(SignUpRequest request) throws Exception {
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request)))
			.andExpect(status().isOk());
	}

	private AuthTokensResponse login(String identifier, String password) throws Exception {
		String response = mockMvc.perform(post("/auth/login")
				.header(HttpHeaders.AUTHORIZATION, basic(identifier, password)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		return objectMapper.readValue(response, AuthTokensResponse.class);
	}

	private void assertHasTokens(AuthTokensResponse response) {
		assertThat(response.sessionToken()).isNotBlank();
		assertThat(response.sessionTokenExpiresAt()).isNotNull();
		assertThat(response.refreshToken()).isNotBlank();
		assertThat(response.refreshTokenExpiresAt()).isNotNull();
	}

}
