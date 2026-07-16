package com.fedesan14.expin_backend.events.data.model;

import com.fedesan14.expin_backend.events.components.settlement_calculator.enums.EventSettlementStrategy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EventSettlement {

    @Enumerated(EnumType.STRING)
	private EventSettlementStrategy strategy;
	BigDecimal totalAmount;
	int participantCount;
    @OneToMany(cascade = CascadeType.ALL)
    Set<EventParticipantBalance> balances;
	@OneToMany(cascade = CascadeType.ALL)
    Set<EventTransfer> transfers;
    @Id
    private final UUID id = UUID.randomUUID();

    public EventSettlement(EventSettlementStrategy strategy, BigDecimal totalAmount, int participantCount, Set<EventParticipantBalance> balances, Set<EventTransfer> transfers) {
        this.strategy = strategy;
        this.totalAmount = totalAmount;
        this.participantCount = participantCount;
        this.balances = balances;
        this.transfers = transfers;
    }
}
