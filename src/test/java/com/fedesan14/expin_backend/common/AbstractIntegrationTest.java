package com.fedesan14.expin_backend.common;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fedesan14.expin_backend.ExpinBackendApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
	classes = ExpinBackendApplication.class,
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	protected String basic(String identifier, String password) {
		String credentials = identifier + ":" + password;
		String encodedCredentials = Base64.getEncoder()
			.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
		return "Basic " + encodedCredentials;
	}

	protected String toJson(Object request) {
		return objectMapper.writeValueAsString(request);
	}
}
