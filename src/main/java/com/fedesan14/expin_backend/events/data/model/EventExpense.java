package com.fedesan14.expin_backend.events.data.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "paid_by_participant_id", nullable = false)
	private EventParticipant paidByParticipant;

	public EventExpense(String title, String description, BigDecimal amount, EventParticipant paidByParticipant) {
		this.id = UUID.randomUUID();
		updateDetails(title, description, amount, paidByParticipant);
	}

	public void updateDetails(String title, String description, BigDecimal amount, EventParticipant paidByParticipant) {
		this.title = title.trim();
		this.description = trimToNull(description);
		this.amount = amount;
		this.paidByParticipant = paidByParticipant;
	}

	private String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
