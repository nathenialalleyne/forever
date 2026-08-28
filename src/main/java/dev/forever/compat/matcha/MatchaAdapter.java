package dev.forever.compat.matcha;

/**
 * Stable Forever-facing Matcha compatibility boundary.
 *
 * <p>No Matcha datapack identifier, resource object, or client type appears in
 * this contract's return values.
 */
public interface MatchaAdapter extends MatchaItemIdentityCapability, MatchaBehaviorCapability {
	boolean supports(MatchaCapability capability);
}
