package dev.forever.core.economy.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.forever.core.economy.application.EconomyBalanceAccess;
import dev.forever.core.economy.domain.CoinPurseState;
import dev.forever.core.economy.domain.EconomyBalance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EconomyCodecTest {

	@Test
	@DisplayName("Coin Purse codec round-trips balance and transaction revision")
	void purseRoundTrip() {
		CoinPurseState original = new CoinPurseState(CoinPurseState.CURRENT_SCHEMA, 731L, 42L);

		JsonElement encoded = CoinPurseState.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		CoinPurseState decoded = CoinPurseState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(original, decoded);
	}

	@Test
	@DisplayName("schema one purse data migrates with a zero transaction revision")
	void purseSchemaOneMigrates() {
		CoinPurseState old = new CoinPurseState(1, 731L, 99L);
		JsonElement encoded = CoinPurseState.rawCodec().encodeStart(JsonOps.INSTANCE, old).getOrThrow();

		CoinPurseState migrated = CoinPurseState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(CoinPurseState.CURRENT_SCHEMA, migrated.schemaVersion());
		assertEquals(old.balance(), migrated.balance());
		assertEquals(0L, migrated.lastTransactionSequence());
	}

	@Test
	@DisplayName("a future purse schema is rejected instead of coerced")
	void purseFutureSchemaRejected() {
		CoinPurseState future = new CoinPurseState(CoinPurseState.CURRENT_SCHEMA + 1, 1L, 1L);
		JsonElement encoded = CoinPurseState.rawCodec().encodeStart(JsonOps.INSTANCE, future).getOrThrow();

		DataResult<CoinPurseState> result = CoinPurseState.CODEC.parse(JsonOps.INSTANCE, encoded);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().orElseThrow().message().contains("restore a backup"));
	}

	@Test
	@DisplayName("bundled economy balance is data-defined")
	void bundledBalanceLoads() {
		EconomyBalanceLoader.installBundled();

		EconomyBalance balance = EconomyBalanceAccess.requireCurrent();

		assertEquals(1_000_000L, balance.maxPurseBalance());
		assertEquals(4_096L, balance.maxTransaction());
		assertEquals(64, balance.physicalStackLimit());
	}
}
