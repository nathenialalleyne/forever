package dev.forever.core.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PurseServiceTest {

	private static final EconomyBalance BALANCE = new EconomyBalance(
			EconomyBalance.CURRENT_SCHEMA, 100_000L, 500L, 64, 1L);

	@Test
	@DisplayName("interleaved deposit and withdrawal sequences conserve total value exactly")
	void propertyStyleConservation() {
		Random random = new Random(0xF506L);
		CoinPurseState state = CoinPurseState.empty();
		long physicalValue = 25_000L;
		long initialTotal = physicalValue + state.balance();

		for (int operation = 0; operation < 2_000; operation++) {
			long amount = 1L + random.nextInt(500);
			long sequence = state.nextTransactionSequence();
			if (random.nextBoolean() && physicalValue >= amount) {
				PurseTransactionResult result = PurseService.deposit(state, sequence, amount, BALANCE);
				assertTrue(result.applied());
				state = result.state();
				physicalValue -= amount;
			} else if (state.balance() >= amount) {
				PurseTransactionResult result = PurseService.withdraw(state, sequence, amount, BALANCE);
				assertTrue(result.applied());
				state = result.state();
				physicalValue += amount;
			}
			assertEquals(initialTotal, physicalValue + state.balance());
		}
	}

	@Test
	@DisplayName("replayed and out-of-order requests do not mutate the purse")
	void replayProtection() {
		CoinPurseState original = CoinPurseState.empty();
		PurseTransactionResult applied = PurseService.deposit(original, 1L, 20L, BALANCE);
		CoinPurseState state = applied.state();

		PurseTransactionResult replay = PurseService.deposit(state, 1L, 20L, BALANCE);
		PurseTransactionResult outOfOrder = PurseService.withdraw(state, 3L, 5L, BALANCE);

		assertFalse(replay.applied());
		assertEquals(PurseTransactionStatus.REPLAYED, replay.status());
		assertEquals(state, replay.state());
		assertFalse(outOfOrder.applied());
		assertEquals(PurseTransactionStatus.OUT_OF_ORDER, outOfOrder.status());
		assertEquals(state, outOfOrder.state());
	}

	@Test
	@DisplayName("limits, insufficient funds, and arithmetic overflow fail closed")
	void failureCasesPreserveValue() {
		CoinPurseState state = CoinPurseState.empty();
		PurseTransactionResult tooLarge = PurseService.deposit(state, 1L, 501L, BALANCE);
		assertFalse(tooLarge.applied());
		assertEquals(state, tooLarge.state());

		PurseTransactionResult insufficient = PurseService.withdraw(state, 1L, 1L, BALANCE);
		assertFalse(insufficient.applied());
		assertEquals(state, insufficient.state());

		EconomyBalance huge = new EconomyBalance(
				EconomyBalance.CURRENT_SCHEMA, Long.MAX_VALUE, Long.MAX_VALUE, 64, 1L);
		CoinPurseState nearMax = new CoinPurseState(CoinPurseState.CURRENT_SCHEMA, Long.MAX_VALUE - 1L, 0L);
		PurseTransactionResult overflow = PurseService.deposit(nearMax, 1L, 2L, huge);
		assertFalse(overflow.applied());
		assertEquals(nearMax, overflow.state());
	}
}
