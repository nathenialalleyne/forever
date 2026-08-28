package dev.forever.compat.matcha;

/** Core-facing behaviour observation boundary for an audited Matcha profile. */
public interface MatchaBehaviorCapability {
	MatchaAdapterStatus status();

	MatchaBehaviorTranslation translateBehavior(MatchaBehaviorObservation observation);
}
