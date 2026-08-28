package dev.forever.compat.matcha;

import java.util.Objects;

/** Neutral Matcha adapter used when the pack is absent or cannot be verified. */
public final class NoOpMatchaAdapter implements MatchaAdapter {
	private final MatchaAdapterStatus status;

	public NoOpMatchaAdapter(MatchaAdapterStatus status) {
		this.status = Objects.requireNonNull(status, "status must not be null.");
	}

	public static NoOpMatchaAdapter absent() {
		return new NoOpMatchaAdapter(MatchaAdapterStatus.fromDetection(MatchaDetectionResult.absent()));
	}

	public static NoOpMatchaAdapter fromDetection(MatchaDetectionResult result) {
		return new NoOpMatchaAdapter(MatchaAdapterStatus.fromDetection(result));
	}

	@Override
	public MatchaAdapterStatus status() {
		return status;
	}

	@Override
	public boolean supports(MatchaCapability capability) {
		return false;
	}

	@Override
	public MatchaItemTranslation translateItem(MatchaItemObservation observation) {
		if (observation == null) {
			return MatchaItemTranslation.invalid("No item observation was supplied; the original item was not changed.");
		}
		return MatchaItemTranslation.unavailable(
				observation,
				"Matcha item identity is unavailable; preserve the original vanilla item unchanged.");
	}

	@Override
	public MatchaBehaviorTranslation translateBehavior(MatchaBehaviorObservation observation) {
		if (observation == null) {
			return MatchaBehaviorTranslation.invalid("No behaviour observation was supplied; no event was emitted.");
		}
		return MatchaBehaviorTranslation.unavailable(
				"Matcha behaviour is unavailable; emit no compatibility event and retain vanilla handling.");
	}
}
