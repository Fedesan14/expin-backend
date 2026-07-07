package com.fedesan14.expin_backend.events.data.model;

import java.util.UUID;

import com.fedesan14.expin_backend.auth.data.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "event_participants")
public class EventParticipant {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column
	private String guestName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ParticipantType type;

	public static EventParticipant user(User user) {
		EventParticipant participant = new EventParticipant();
		participant.id = UUID.randomUUID();
		participant.user = user;
		participant.type = ParticipantType.USER;
		return participant;
	}

	public static EventParticipant guest(String guestName) {
		EventParticipant participant = new EventParticipant();
		participant.id = UUID.randomUUID();
		participant.guestName = guestName.trim();
		participant.type = ParticipantType.GUEST;
		return participant;
	}

	public boolean isUser(UUID userId) {
		return type == ParticipantType.USER && user != null && user.getId().equals(userId);
	}

	public boolean isSameIdentity(EventParticipant other) {
		if (type != other.type) {
			return false;
		}
		if (type == ParticipantType.USER) {
			return user.getId().equals(other.user.getId());
		}
		return guestName.equalsIgnoreCase(other.guestName);
	}

	public String displayName() {
		if (type == ParticipantType.USER) {
			return user.getUsername();
		}
		return guestName;
	}
}
