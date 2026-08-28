package dev.forever.core.career;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/** Mason-facing name for the shared persistent career attachment. */
public final class MasonCareerAttachment {

	public static final AttachmentType<CareerState> TYPE = CareerAttachment.TYPE;

	private MasonCareerAttachment() {
	}

	public static void initialize() {
		CareerAttachment.initialize();
	}
}
