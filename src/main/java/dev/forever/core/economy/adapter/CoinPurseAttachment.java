package dev.forever.core.economy.adapter;

import dev.forever.core.economy.domain.CoinPurseState;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/** Persistent player attachment for the withdrawable Coin Purse balance. */
public final class CoinPurseAttachment {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("forever", "coin_purse");
	public static final AttachmentType<CoinPurseState> TYPE = AttachmentRegistry.<CoinPurseState>builder()
			.persistent(CoinPurseState.CODEC)
			.copyOnDeath()
			.initializer(CoinPurseState::empty)
			.buildAndRegister(ID);

	private CoinPurseAttachment() {
	}

	public static void initialize() {
		TYPE.identifier();
	}
}
