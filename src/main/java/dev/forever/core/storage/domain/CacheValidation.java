package dev.forever.core.storage.domain;

/** Result of validating a cache ItemStack against the active server balance. */
public record CacheValidation(boolean valid, String explanationKey) {

    public static CacheValidation accepted() {
        return new CacheValidation(true, "");
    }

	public static CacheValidation invalid(String explanationKey) {
		return new CacheValidation(false, explanationKey);
	}
}
