package dev.forever.core.career.adapter;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/** Persistent entity attachment containing one villager's compact career state. */
public final class CareerAttachment {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("forever", "career_state");
	public static final AttachmentType<CareerState> TYPE = AttachmentRegistry.<CareerState>builder()
			.persistent(CareerState.CODEC)
			.initializer(CareerState::empty)
			.buildAndRegister(ID);

	private CareerAttachment() {
	}

	/** Forces registration from the explicit career system entrypoint. */
	public static void initialize() {
		TYPE.identifier();
	}
}
