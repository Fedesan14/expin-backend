package com.fedesan14.expin_backend.events.data.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fedesan14.expin_backend.users.data.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "SharedEvent")
@Table(name = "events")
public class Event {

	private static final int MONEY_SCALE = 2;

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(nullable = false)
	private String title;

	@Column
	private String description;

	@Column
	private LocalDate startDate;

	@Column
	private LocalDate endDate;

	@Column(nullable = false, unique = true)
	private String shareLink;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<EventParticipant> participants = new LinkedHashSet<>();

	@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<EventExpense> expenses = new LinkedHashSet<>();

	public Event(String title, String description, LocalDate startDate, LocalDate endDate, String shareLink, User owner) {
		this.id = UUID.randomUUID();
		this.shareLink = shareLink;
		this.owner = owner;
		updateDetails(title, description, startDate, endDate);
	}

	public void updateDetails(String title, String description, LocalDate startDate, LocalDate endDate) {
		this.title = title.trim();
		this.description = trimToNull(description);
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void addParticipant(EventParticipant participant) {
		participant.setEvent(this);
		participants.add(participant);
	}

	public void removeParticipant(EventParticipant participant) {
		participants.remove(participant);
		participant.setEvent(null);
	}

	public void addExpense(EventExpense expense) {
		expense.setEvent(this);
		expenses.add(expense);
	}

	public void removeExpense(EventExpense expense) {
		expenses.remove(expense);
		expense.setEvent(null);
	}

	public boolean isOwner(User user) {
		return owner.getId().equals(user.getId());
	}

	public boolean hasUserParticipant(UUID userId) {
		return participants.stream()
			.anyMatch(participant -> participant.isUser(userId));
	}

	public Optional<EventParticipant> findParticipant(UUID participantId) {
		return participants.stream()
			.filter(participant -> participant.getId().equals(participantId))
			.findFirst();
	}

	public BigDecimal calculateEventTotalAmount() {
		return expenses.stream()
			.map(EventExpense::getAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
	}

	private String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
