package com.fedesan14.expin_backend.events.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EventTransfer {
	private UUID fromParticipantId;
	private UUID toParticipantId;
	private String fromDisplayName;
	private String toDisplayName;
	private BigDecimal amount;
    @Id
    private final UUID id = UUID.randomUUID();

    public EventTransfer(UUID fromParticipantId, String fromDisplayName, UUID toParticipantId, String toDisplayName, BigDecimal amount) {
        this.fromParticipantId = fromParticipantId;
        this.toParticipantId = toParticipantId;
        this.fromDisplayName = fromDisplayName;
        this.toDisplayName = toDisplayName;
        this.amount = amount;
    }
}
