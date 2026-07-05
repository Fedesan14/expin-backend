package com.fedesan14.expin_backend.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import com.fedesan14.expin_backend.ExpinBackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
	classes = ExpinBackendApplication.class,
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
class AuthFlowIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void signupCreatesUser() throws Exception {
		String suffix = suffix();

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "username": "user-%s",
					  "password": "password123",
					  "email": "user-%s@example.com"
					}
					""".formatted(suffix, suffix)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.username").value("user-" + suffix))
			.andExpect(jsonPath("$.profileId").isNotEmpty())
			.andExpect(jsonPath("$.email").value("user-" + suffix + "@example.com"))
			.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void signupRejectsDuplicatedUsername() throws Exception {
		String suffix = suffix();
		String username = "duplicated-username-" + suffix;

		signup(username, "first-" + suffix + "@example.com");

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(signupBody(username, "second-" + suffix + "@example.com")))
			.andExpect(status().isConflict());
	}

	@Test
	void signupRejectsDuplicatedEmail() throws Exception {
		String suffix = suffix();
		String email = "duplicated-email-" + suffix + "@example.com";

		signup("first-" + suffix, email);

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(signupBody("second-" + suffix, email)))
			.andExpect(status().isConflict());
	}

	@Test
	void loginAcceptsUsernameAndPassword() throws Exception {
		String suffix = suffix();
		String username = "login-username-" + suffix;
		signup(username, "login-username-" + suffix + "@example.com");

		JsonNode response = login(username, "password123");

		assertHasTokens(response);
	}

	@Test
	void loginAcceptsEmailAndPassword() throws Exception {
		String suffix = suffix();
		String username = "login-email-" + suffix;
		String email = "login-email-" + suffix + "@example.com";
		signup(username, email);

		JsonNode response = login(email, "password123");

		assertHasTokens(response);
	}

	@Test
	void privateEndpointRejectsMissingBearerToken() throws Exception {
		mockMvc.perform(get("/ping"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void privateEndpointAcceptsSessionToken() throws Exception {
		String suffix = suffix();
		String username = "private-" + suffix;
		signup(username, "private-" + suffix + "@example.com");
		String sessionToken = login(username, "password123").get("sessionToken").asText();

		mockMvc.perform(get("/ping")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + sessionToken))
			.andExpect(status().isOk())
			.andExpect(content().string("pong"));
	}

	private void signup(String username, String email) throws Exception {
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(signupBody(username, email)))
			.andExpect(status().isOk());
	}

	private String signupBody(String username, String email) {
		return """
			{
			  "username": "%s",
			  "password": "password123",
			  "email": "%s"
			}
			""".formatted(username, email);
	}

	private String suffix() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	private JsonNode login(String identifier, String password) throws Exception {
		String response = mockMvc.perform(post("/auth/login")
				.header(HttpHeaders.AUTHORIZATION, basic(identifier, password)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		return objectMapper.readTree(response);
	}

	private String basic(String identifier, String password) {
		String credentials = identifier + ":" + password;
		String encodedCredentials = Base64.getEncoder()
			.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
		return "Basic " + encodedCredentials;
	}

	private void assertHasTokens(JsonNode response) {
		assertThat(response.get("sessionToken").asText()).isNotBlank();
		assertThat(response.get("sessionTokenExpiresAt").asText()).isNotBlank();
		assertThat(response.get("refreshToken").asText()).isNotBlank();
		assertThat(response.get("refreshTokenExpiresAt").asText()).isNotBlank();
	}

}
