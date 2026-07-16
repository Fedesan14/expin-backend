package com.fedesan14.expin_backend.events.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "event_expenses")
public class EventExpense {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@Column(nullable = false)
	private String title;

	@Column
	private String description;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

    private LocalDateTime createdAt = LocalDateTime.now();

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "paid_by_participant_id", nullable = false)
	private EventParticipant paidByParticipant;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
		name = "event_expense_debtors",
		joinColumns = @JoinColumn(name = "expense_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "participant_id", nullable = false)
	)
	private Set<EventParticipant> owedByParticipants = new LinkedHashSet<>();

	public EventExpense(
		String title,
		String description,
		BigDecimal amount,
		EventParticipant paidByParticipant,
		Set<EventParticipant> owedByParticipants
	) {
		this.id = UUID.randomUUID();
		updateDetails(title, description, amount, paidByParticipant, owedByParticipants);
	}

	public void updateDetails(
		String title,
		String description,
		BigDecimal amount,
		EventParticipant paidByParticipant,
		Set<EventParticipant> owedByParticipants
	) {
		this.title = title.trim();
		this.description = trimToNull(description);
		this.amount = amount;
		this.paidByParticipant = paidByParticipant;
		this.owedByParticipants.clear();
		this.owedByParticipants.addAll(owedByParticipants);
	}

	private String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
