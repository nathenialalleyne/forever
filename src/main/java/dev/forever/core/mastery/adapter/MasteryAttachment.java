package dev.forever.core.mastery.adapter;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/** Persistent player attachment for permanent mastery learning. */
public final class MasteryAttachment {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("forever", "mastery_state");

	/**
	 * The builder deliberately includes copyOnDeath. Mastery learning is permanent even
	 * when vanilla creates a replacement player entity after death.
	 */
	public static final AttachmentType<MasteryState> TYPE = AttachmentRegistry.<MasteryState>builder()
			.persistent(MasteryState.CODEC)
			.copyOnDeath()
			.initializer(MasteryState::empty)
			.buildAndRegister(ID);

	private MasteryAttachment() {
	}

	/** Forces class initialisation from the system entrypoint without exposing mutable state. */
	public static void initialize() {
		TYPE.identifier();
	}
}
