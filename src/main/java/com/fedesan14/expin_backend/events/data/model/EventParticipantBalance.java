package com.fedesan14.expin_backend.events.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EventParticipantBalance {
	UUID participantId;
	String displayName;
	BigDecimal paidAmount;
	BigDecimal owedAmount;
	BigDecimal balance;
    @Id
    private final UUID id = UUID.randomUUID();

    public EventParticipantBalance(UUID participantId, String displayName, BigDecimal paidAmount, BigDecimal owedAmount, BigDecimal balance) {
        this.participantId = participantId;
        this.displayName = displayName;
        this.paidAmount = paidAmount;
        this.owedAmount = owedAmount;
        this.balance = balance;
    }
}
