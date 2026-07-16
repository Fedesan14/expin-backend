package com.fedesan14.expin_backend.events.data.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fedesan14.expin_backend.events.data.model.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {

	boolean existsByShareLink(String shareLink);

	@EntityGraph(attributePaths = {
		"owner",
		"participants",
		"participants.user",
		"expenses",
		"expenses.paidByParticipant",
		"expenses.owedByParticipants"
	})
	Optional<Event> findWithDetailsByShareLink(String shareLink);

	@EntityGraph(attributePaths = {"owner", "participants", "participants.user"})
	@Query("""
		SELECT DISTINCT event
		FROM SharedEvent event
		LEFT JOIN event.participants participant
		LEFT JOIN participant.user participantUser
		WHERE event.owner.id = :userId OR participantUser.id = :userId
		""")
	List<Event> findAllVisibleToUser(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {
            "owner",
            "participants",
            "participants.user",
            "expenses",
            "expenses.paidByParticipant",
            "expenses.owedByParticipants",
            "settlement",
            "settlement.balances",
            "settlement.transfers"
    })
	Optional<Event> findWithDetailsById(UUID id);
}
