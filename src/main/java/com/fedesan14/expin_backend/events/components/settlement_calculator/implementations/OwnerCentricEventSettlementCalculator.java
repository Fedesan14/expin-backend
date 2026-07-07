package com.fedesan14.expin_backend.events.components.settlement_calculator.implementations;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fedesan14.expin_backend.events.components.settlement_calculator.interfaces.EventSettlementCalculator;
import com.fedesan14.expin_backend.events.data.model.Event;
import com.fedesan14.expin_backend.events.data.model.EventExpense;
import com.fedesan14.expin_backend.events.data.model.EventParticipant;
import com.fedesan14.expin_backend.events.data.model.EventParticipantBalance;
import com.fedesan14.expin_backend.events.data.model.EventSettlement;
import com.fedesan14.expin_backend.events.data.model.EventSettlementStrategy;
import com.fedesan14.expin_backend.events.data.model.EventTransfer;
import org.springframework.stereotype.Component;

@Component
public class OwnerCentricEventSettlementCalculator implements EventSettlementCalculator {

	private static final int MONEY_SCALE = 2;
	private static final BigDecimal CENTS = new BigDecimal("100");

	@Override
	public EventSettlementStrategy strategy() {
		return EventSettlementStrategy.OWNER_CENTRIC;
	}

	@Override
	public EventSettlement calculate(Event event) {
		List<EventParticipant> participants = new ArrayList<>(event.getParticipants());
		BigDecimal totalAmount = event.calculateEventTotalAmount();

		Map<UUID, BigDecimal> paidAmounts = event.getExpenses().stream()
			.collect(Collectors.groupingBy(
				expense -> expense.getPaidByParticipant().getId(),
				Collectors.reducing(BigDecimal.ZERO, EventExpense::getAmount, BigDecimal::add)
			));
		Map<UUID, BigDecimal> owedAmounts = owedAmounts(totalAmount, participants);
		List<EventParticipantBalance> balances = participants.stream()
			.map(participant -> toBalance(participant, paidAmounts, owedAmounts))
			.toList();

		return new EventSettlement(
			event.getId(),
			strategy(),
			totalAmount,
			participants.size(),
			balances,
			transfersToSettleWithOwner(event, balances)
		);
	}

	private Map<UUID, BigDecimal> owedAmounts(BigDecimal totalAmount, List<EventParticipant> participants) {
		if (participants.isEmpty()) {
			return Map.of();
		}

		BigInteger totalCents = totalAmount.multiply(CENTS).toBigIntegerExact();
		BigInteger participantCount = BigInteger.valueOf(participants.size());
		BigInteger[] division = totalCents.divideAndRemainder(participantCount);
		int remainder = division[1].intValueExact();

		Map<UUID, BigDecimal> amounts = new LinkedHashMap<>();
		for (int index = 0; index < participants.size(); index++) {
			EventParticipant participant = participants.get(index);
			BigInteger participantCents = division[0].add(index < remainder ? BigInteger.ONE : BigInteger.ZERO);
			amounts.put(
				participant.getId(),
				new BigDecimal(participantCents).divide(CENTS, MONEY_SCALE, RoundingMode.UNNECESSARY)
			);
		}
		return amounts;
	}

	private EventParticipantBalance toBalance(
		EventParticipant participant,
		Map<UUID, BigDecimal> paidAmounts,
		Map<UUID, BigDecimal> owedAmounts
	) {
		BigDecimal paidAmount = paidAmounts.getOrDefault(participant.getId(), BigDecimal.ZERO)
			.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		BigDecimal owedAmount = owedAmounts.getOrDefault(participant.getId(), BigDecimal.ZERO)
			.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
		return new EventParticipantBalance(
			participant.getId(),
			participant.displayName(),
			paidAmount,
			owedAmount,
			paidAmount.subtract(owedAmount).setScale(MONEY_SCALE, RoundingMode.HALF_UP)
		);
	}

	private List<EventTransfer> transfersToSettleWithOwner(Event event, List<EventParticipantBalance> balances) {
		EventParticipant ownerParticipant = event.getParticipants().stream()
			.filter(participant -> participant.isUser(event.getOwner().getId()))
			.findFirst()
			.orElseThrow();
		EventParticipantBalance ownerBalance = balances.stream()
			.filter(balance -> balance.participantId().equals(ownerParticipant.getId()))
			.findFirst()
			.orElseThrow();

		List<EventTransfer> transfers = new ArrayList<>();
		for (EventParticipantBalance balance : balances) {
			if (balance.participantId().equals(ownerParticipant.getId()) || balance.balance().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			if (balance.balance().compareTo(BigDecimal.ZERO) < 0) {
				transfers.add(new EventTransfer(
					balance.participantId(),
					balance.displayName(),
					ownerBalance.participantId(),
					ownerBalance.displayName(),
					balance.balance().abs()
				));
			} else {
				transfers.add(new EventTransfer(
					ownerBalance.participantId(),
					ownerBalance.displayName(),
					balance.participantId(),
					balance.displayName(),
					balance.balance()
				));
			}
		}
		return transfers;
	}
}
