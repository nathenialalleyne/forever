package dev.forever.compat.matcha;

/** Core-facing item identity boundary for an audited Matcha profile. */
public interface MatchaItemIdentityCapability {
	MatchaAdapterStatus status();

	MatchaItemTranslation translateItem(MatchaItemObservation observation);
}
