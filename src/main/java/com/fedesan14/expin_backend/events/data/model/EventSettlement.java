package com.fedesan14.expin_backend.events.data.model;

import com.fedesan14.expin_backend.events.components.settlement_calculator.enums.EventSettlementStrategy;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EventSettlement {

    @ManyToOne
	private Event event;
    @Enumerated(EnumType.STRING)
	private EventSettlementStrategy strategy;
	BigDecimal totalAmount;
	int participantCount;
    @OneToMany
	List<EventParticipantBalance> balances;
	@OneToMany
    List<EventTransfer> transfers;
    @Id
    private final UUID id = UUID.randomUUID();

    public EventSettlement(Event event, EventSettlementStrategy strategy, BigDecimal totalAmount, int participantCount, List<EventParticipantBalance> balances, List<EventTransfer> transfers) {
        this.event = event;
        this.strategy = strategy;
        this.totalAmount = totalAmount;
        this.participantCount = participantCount;
        this.balances = balances;
        this.transfers = transfers;
    }
}
