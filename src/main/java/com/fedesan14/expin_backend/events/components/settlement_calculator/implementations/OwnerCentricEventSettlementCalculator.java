package com.fedesan14.expin_backend.events.components.settlement_calculator.implementations;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import com.fedesan14.expin_backend.events.components.settlement_calculator.interfaces.EventSettlementCalculator;
import com.fedesan14.expin_backend.events.data.model.Event;
import com.fedesan14.expin_backend.events.data.model.EventExpense;
import com.fedesan14.expin_backend.events.data.model.EventParticipant;
import com.fedesan14.expin_backend.events.data.model.EventParticipantBalance;
import com.fedesan14.expin_backend.events.data.model.EventSettlement;
import com.fedesan14.expin_backend.events.components.settlement_calculator.enums.EventSettlementStrategy;
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
		Map<UUID, BigDecimal> owedAmounts = owedAmounts(event.getExpenses(), participants);
		Set<EventParticipantBalance> balances = participants.stream()
			.map(participant -> toBalance(participant, paidAmounts, owedAmounts))
			.collect(Collectors.toSet());

		return new EventSettlement(
			strategy(),
			totalAmount,
			participants.size(),
			balances,
			transfersToSettleWithOwner(event, balances)
		);
	}

	private Map<UUID, BigDecimal> owedAmounts(Iterable<EventExpense> expenses, List<EventParticipant> eventParticipants) {
		Map<UUID, BigDecimal> amounts = new LinkedHashMap<>();
		for (EventExpense expense : expenses) {
			List<EventParticipant> debtors = new ArrayList<>(expense.getOwedByParticipants());
			if (debtors.isEmpty()) {
				debtors = eventParticipants;
			}
			BigInteger expenseCents = expense.getAmount().multiply(CENTS).toBigIntegerExact();
			BigInteger debtorCount = BigInteger.valueOf(debtors.size());
			BigInteger[] division = expenseCents.divideAndRemainder(debtorCount);
			int remainder = division[1].intValueExact();

			for (int index = 0; index < debtors.size(); index++) {
				EventParticipant debtor = debtors.get(index);
				BigInteger debtorCents = division[0].add(index < remainder ? BigInteger.ONE : BigInteger.ZERO);
				BigDecimal owedAmount = new BigDecimal(debtorCents)
					.divide(CENTS, MONEY_SCALE, RoundingMode.UNNECESSARY);
				amounts.merge(debtor.getId(), owedAmount, BigDecimal::add);
			}
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

	private Set<EventTransfer> transfersToSettleWithOwner(Event event, Set<EventParticipantBalance> balances) {
		EventParticipant ownerParticipant = event.getParticipants().stream()
			.filter(participant -> participant.isUser(event.getOwner().getId()))
			.findFirst()
			.orElseThrow();
		EventParticipantBalance ownerBalance = balances.stream()
			.filter(balance -> balance.getParticipantId().equals(ownerParticipant.getId()))
			.findFirst()
			.orElseThrow();

		Set<EventTransfer> transfers = new HashSet<>();
		for (EventParticipantBalance balance : balances) {
			if (balance.getParticipantId().equals(ownerParticipant.getId()) || balance.getBalance().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			if (balance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
				transfers.add(new EventTransfer(
					balance.getParticipantId(),
					balance.getDisplayName(),
					ownerBalance.getParticipantId(),
					ownerBalance.getDisplayName(),
					balance.getBalance().abs()
				));
			} else {
				transfers.add(new EventTransfer(
					ownerBalance.getParticipantId(),
					ownerBalance.getDisplayName(),
					balance.getParticipantId(),
					balance.getDisplayName(),
					balance.getBalance()
				));
			}
		}
		return transfers;
	}
}
