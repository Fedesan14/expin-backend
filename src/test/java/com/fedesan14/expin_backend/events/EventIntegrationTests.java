package com.fedesan14.expin_backend.events;

import java.util.UUID;

import com.fedesan14.expin_backend.auth.AuthDataMock;
import com.fedesan14.expin_backend.auth.controller.requests.SignUpRequest;
import com.fedesan14.expin_backend.auth.controller.responses.AuthTokensResponse;
import com.fedesan14.expin_backend.auth.controller.responses.UserResponse;
import com.fedesan14.expin_backend.common.AbstractIntegrationTest;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.CreateEventRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventExpenseRequest;
import com.fedesan14.expin_backend.events.controller.requests.UpdateEventRequest;
import com.fedesan14.expin_backend.events.controller.responses.EventExpenseResponse;
import com.fedesan14.expin_backend.events.controller.responses.EventParticipantResponse;
import com.fedesan14.expin_backend.events.controller.responses.EventResponse;
import com.fedesan14.expin_backend.events.data.model.ParticipantType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventIntegrationTests extends AbstractIntegrationTest {

	@Test
	void createEventAddsOwnerAsParticipantAndGeneratesRelativeShareLink() throws Exception {
		TestUser owner = createUser("event-owner");
		CreateEventRequest request = EventDataMock.eventWithGuest("create");

		EventResponse response = createEvent(owner.sessionToken(), request);

		assertThat(response.id()).isNotNull();
		assertThat(response.ownerId()).isEqualTo(owner.id());
		assertThat(response.shareLink()).startsWith("/events/invite/");
		assertThat(response.shareLink()).doesNotStartWith("http");
		assertThat(response.participants())
			.anyMatch(participant -> participant.type() == ParticipantType.USER && participant.userId().equals(owner.id()));
		assertThat(response.participants())
			.anyMatch(participant -> participant.type() == ParticipantType.GUEST && participant.guestName() != null);
		assertThat(response.expenses()).isEmpty();
	}

	@Test
	void listEventsReturnsOnlyEventsWhereUserParticipates() throws Exception {
		TestUser owner = createUser("list-owner");
		TestUser participant = createUser("list-participant");
		TestUser outsider = createUser("list-outsider");

		EventResponse visibleEvent = createEvent(
			owner.sessionToken(),
			EventDataMock.eventWithUser("visible", participant.id())
		);
		createEvent(owner.sessionToken(), EventDataMock.eventWithGuest("hidden"));

		mockMvc.perform(get("/events")
				.header(HttpHeaders.AUTHORIZATION, bearer(participant.sessionToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[?(@.id == '%s')]".formatted(visibleEvent.id())).exists());

		mockMvc.perform(get("/events")
				.header(HttpHeaders.AUTHORIZATION, bearer(outsider.sessionToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void getEventReturnsParticipantsAndExpenses() throws Exception {
		TestUser owner = createUser("detail-owner");
		EventResponse event = createEvent(owner.sessionToken(), EventDataMock.eventWithGuest("detail"));
		UUID ownerParticipantId = ownerParticipantId(event, owner.id());
		EventExpenseResponse expense = createExpense(
			owner.sessionToken(),
			event.id(),
			EventDataMock.expense(ownerParticipantId)
		);

		mockMvc.perform(get("/events/{eventId}", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(owner.sessionToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.participants").isNotEmpty())
			.andExpect(jsonPath("$.expenses[0].id").value(expense.id().toString()));
	}

	@Test
	void userThatDoesNotParticipateCannotGetEvent() throws Exception {
		TestUser owner = createUser("forbidden-owner");
		TestUser outsider = createUser("forbidden-outsider");
		EventResponse event = createEvent(owner.sessionToken(), EventDataMock.eventWithGuest("forbidden"));

		mockMvc.perform(get("/events/{eventId}", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(outsider.sessionToken())))
			.andExpect(status().isForbidden());
	}

	@Test
	void authenticatedUserCanJoinEventUsingShareLink() throws Exception {
		TestUser owner = createUser("invite-owner");
		TestUser invitedUser = createUser("invite-user");
		EventResponse event = createEvent(owner.sessionToken(), EventDataMock.eventWithGuest("invite"));

		mockMvc.perform(post(event.shareLink())
				.header(HttpHeaders.AUTHORIZATION, bearer(invitedUser.sessionToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(event.id().toString()))
			.andExpect(jsonPath("$.participants[?(@.userId == '%s')]".formatted(invitedUser.id())).exists());

		mockMvc.perform(get("/events/{eventId}", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(invitedUser.sessionToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(event.id().toString()));

		String secondJoinResponse = mockMvc.perform(post(event.shareLink())
				.header(HttpHeaders.AUTHORIZATION, bearer(invitedUser.sessionToken())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		EventResponse joinedAgainEvent = objectMapper.readValue(secondJoinResponse, EventResponse.class);

		assertThat(joinedAgainEvent.participants())
			.filteredOn(participant -> invitedUser.id().equals(participant.userId()))
			.hasSize(1);
	}

	@Test
	void unknownShareLinkCannotBeUsedToJoinEvent() throws Exception {
		TestUser user = createUser("invite-missing");

		mockMvc.perform(post("/events/invite/missing-token")
				.header(HttpHeaders.AUTHORIZATION, bearer(user.sessionToken())))
			.andExpect(status().isNotFound());
	}

	@Test
	void onlyOwnerCanUpdateEvent() throws Exception {
		TestUser owner = createUser("update-owner");
		TestUser participant = createUser("update-participant");
		EventResponse event = createEvent(
			owner.sessionToken(),
			EventDataMock.eventWithUser("update", participant.id())
		);
		UpdateEventRequest request = EventDataMock.updateWithGuest("update");

		mockMvc.perform(put("/events/{eventId}", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(participant.sessionToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request)))
			.andExpect(status().isForbidden());

		mockMvc.perform(put("/events/{eventId}", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(owner.sessionToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value(request.title()))
			.andExpect(jsonPath("$.shareLink").value(event.shareLink()));
	}

	@Test
	void onlyOwnerCanDeleteEvent() throws Exception {
		TestUser owner = createUser("delete-owner");
		TestUser participant = createUser("delete-participant");
		EventResponse event = createEvent(
			owner.sessionToken(),
			EventDataMock.eventWithUser("delete", participant.id())
		);

		mockMvc.perform(delete("/events/{eventId}", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(participant.sessionToken())))
			.andExpect(status().isForbidden());

		mockMvc.perform(delete("/events/{eventId}", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(owner.sessionToken())))
			.andExpect(status().isNoContent());
	}

	@Test
	void createExpenseRequiresParticipantAndValidPayer() throws Exception {
		TestUser owner = createUser("expense-owner");
		TestUser outsider = createUser("expense-outsider");
		EventResponse event = createEvent(owner.sessionToken(), EventDataMock.eventWithGuest("expense"));
		UUID ownerParticipantId = ownerParticipantId(event, owner.id());

		createExpense(owner.sessionToken(), event.id(), EventDataMock.expense(ownerParticipantId));

		mockMvc.perform(post("/events/{eventId}/expenses", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(owner.sessionToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(EventDataMock.invalidAmountExpense(ownerParticipantId))))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/events/{eventId}/expenses", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(owner.sessionToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(EventDataMock.expense(UUID.randomUUID()))))
			.andExpect(status().isNotFound());

		mockMvc.perform(post("/events/{eventId}/expenses", event.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(outsider.sessionToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(EventDataMock.expense(ownerParticipantId))))
			.andExpect(status().isForbidden());
	}

	@Test
	void getUpdateAndDeleteExpenseInsideEvent() throws Exception {
		TestUser owner = createUser("expense-crud-owner");
		EventResponse event = createEvent(owner.sessionToken(), EventDataMock.eventWithGuest("expense-crud"));
		UUID ownerParticipantId = ownerParticipantId(event, owner.id());
		EventExpenseResponse expense = createExpense(
			owner.sessionToken(),
			event.id(),
			EventDataMock.expense(ownerParticipantId)
		);
		UpdateEventExpenseRequest updateRequest = EventDataMock.updateExpense(ownerParticipantId);

		mockMvc.perform(get("/events/{eventId}/expenses/{expenseId}", event.id(), expense.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(owner.sessionToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(expense.id().toString()));

		mockMvc.perform(put("/events/{eventId}/expenses/{expenseId}", event.id(), expense.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(owner.sessionToken()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(updateRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value(updateRequest.title()))
			.andExpect(jsonPath("$.amount").value(50000.00));

		mockMvc.perform(delete("/events/{eventId}/expenses/{expenseId}", event.id(), expense.id())
				.header(HttpHeaders.AUTHORIZATION, bearer(owner.sessionToken())))
			.andExpect(status().isNoContent());
	}

	private EventResponse createEvent(String sessionToken, CreateEventRequest request) throws Exception {
		String response = mockMvc.perform(post("/events")
				.header(HttpHeaders.AUTHORIZATION, bearer(sessionToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		return objectMapper.readValue(response, EventResponse.class);
	}

	private EventExpenseResponse createExpense(
		String sessionToken,
		UUID eventId,
		CreateEventExpenseRequest request
	) throws Exception {
		String response = mockMvc.perform(post("/events/{eventId}/expenses", eventId)
				.header(HttpHeaders.AUTHORIZATION, bearer(sessionToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		return objectMapper.readValue(response, EventExpenseResponse.class);
	}

	private TestUser createUser(String prefix) throws Exception {
		SignUpRequest request = new SignUpRequest(
			prefix + "-" + UUID.randomUUID().toString().substring(0, 8),
			AuthDataMock.VALID_PASSWORD,
			prefix + "-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com"
		);

		String signupResponse = mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(toJson(request)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		UserResponse user = objectMapper.readValue(signupResponse, UserResponse.class);

		String loginResponse = mockMvc.perform(post("/auth/login")
				.header(HttpHeaders.AUTHORIZATION, basic(request.username(), request.password())))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		AuthTokensResponse tokens = objectMapper.readValue(loginResponse, AuthTokensResponse.class);
		return new TestUser(user.id(), tokens.sessionToken());
	}

	private UUID ownerParticipantId(EventResponse event, UUID ownerId) {
		return event.participants().stream()
			.filter(participant -> participant.userId() != null && participant.userId().equals(ownerId))
			.map(EventParticipantResponse::id)
			.findFirst()
			.orElseThrow();
	}

	private String bearer(String sessionToken) {
		return "Bearer " + sessionToken;
	}

	private record TestUser(UUID id, String sessionToken) {
	}
}
