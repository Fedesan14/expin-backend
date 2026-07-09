package com.fedesan14.expin_backend.events.data.repository;

import java.util.Optional;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.model.EventExpense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventExpenseRepository extends JpaRepository<EventExpense, UUID> {

	@EntityGraph(attributePaths = {
		"event",
		"event.owner",
		"event.participants",
		"event.participants.user",
		"paidByParticipant",
		"owedByParticipants"
	})
	Optional<EventExpense> findByIdAndEventId(UUID id, UUID eventId);
}
