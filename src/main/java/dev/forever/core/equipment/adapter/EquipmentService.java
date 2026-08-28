package dev.forever.core.equipment;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Server-side operations for condition, repair, and reforge transactions. */
public final class EquipmentService {

	private static final Logger LOGGER = LoggerFactory.getLogger("forever/equipment");

	public static final String INVALID_ITEM = "message.forever.equipment.invalid_item";
	public static final String INVALID_STATE = "message.forever.equipment.invalid_state";
	public static final String BALANCE_UNAVAILABLE = "message.forever.equipment.balance_unavailable";
	public static final String ALREADY_FULL = "message.forever.equipment.already_full";
	public static final String MISSING_MATERIAL = "message.forever.equipment.missing_material";
	public static final String WORKSHOP_REQUIRED = "message.forever.equipment.workshop_required";
	public static final String UNKNOWN_PATH = "message.forever.equipment.unknown_path";
	public static final String SAME_PATH = "message.forever.equipment.same_path";
	public static final String FIELD_REPAIR_COMPLETE = "message.forever.equipment.field_repair_complete";
	public static final String WORKSHOP_REPAIR_COMPLETE = "message.forever.equipment.workshop_repair_complete";
	public static final String REFORGE_COMPLETE = "message.forever.equipment.reforge_complete";

	private EquipmentService() {
	}

	/** Returns the component without creating state or changing the stack. */
	public static Optional<EquipmentState> state(ItemStack stack) {
		Objects.requireNonNull(stack, "stack");
		return Optional.ofNullable(stack.get(EquipmentComponents.STATE));
	}

	/** Returns whether the stack belongs to the Forever-supported item family. */
	public static boolean isSupported(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() instanceof EquipmentItem;
	}

	/** Returns whether the stack may currently be used. Uninitialised equipment is usable. */
	public static boolean isUsable(ItemStack stack) {
		return state(stack).map(current -> !current.isBroken()).orElse(true);
	}

	/**
	 * Creates the component for a newly obtained supported item. The UUID is generated exactly
	 * once and then travels with the ItemStack through copies, drops, and containers.
	 */
	public static EquipmentState initializeStack(ItemStack stack) {
		requireSupported(stack);
		EquipmentState existing = stack.get(EquipmentComponents.STATE);
		if (existing != null) {
			return existing;
		}
		EquipmentBalance balance = EquipmentBalanceRegistry.requireCurrent();
		EquipmentState created = EquipmentState.newItem(balance.maxCondition(), 0);
		setState(stack, created);
		return created;
	}

	/** Explicitly installs a validated state on a supported stack. */
	public static void setState(ItemStack stack, EquipmentState state) {
		requireSupported(stack);
		EquipmentState next = Objects.requireNonNull(state, "state");
		EquipmentBalanceRegistry.current().ifPresent(balance -> requireValidAgainstBalance(next, balance));
		stack.set(EquipmentComponents.STATE, next);
		syncVanillaDamage(stack, next);
	}

	/**
	 * Applies a server-side condition loss. This is also the operation used by the Fabric custom
	 * damage handler, so vanilla's destructive callback is never reached for supported equipment.
	 */
	public static EquipmentState damage(ItemStack stack, int amount) {
		EquipmentState current = ensureState(stack);
		if (amount <= 0 || current.isBroken()) {
			return current;
		}
		EquipmentBalance balance = EquipmentBalanceRegistry.requireCurrent();
		requireValidAgainstBalance(current, balance);
		int applied = EquipmentMath.damageAmount(current, amount, balance);
		EquipmentState updated = current.withCondition(current.currentCondition() - applied);
		setState(stack, updated);
		return updated;
	}

	/** Invoked by {@link EquipmentDamageHandler}; invalid data is retained and made safe. */
	static void applyCustomDamage(ItemStack stack, int amount) {
		try {
			damage(stack, amount);
		} catch (IllegalArgumentException | IllegalStateException exception) {
			LOGGER.error("Could not apply condition damage to supported equipment; retaining the stack: {}",
					exception.getMessage(), exception);
		}
	}

	/** Adds a known path with zero progress, preserving all existing state. */
	public static EquipmentState learnPath(ItemStack stack, Identifier path) {
		EquipmentState current = ensureState(stack);
		EquipmentBalance balance = EquipmentBalanceRegistry.requireCurrent();
		if (balance.path(path).isEmpty()) {
			throw new IllegalArgumentException("cannot learn undefined equipment path " + path);
		}
		EquipmentState updated = current.learnPath(path);
		setState(stack, updated);
		return updated;
	}

	/** Updates retained path progress without changing identity or the active path. */
	public static EquipmentState setPathProgress(ItemStack stack, Identifier path, int progress) {
		EquipmentState current = ensureState(stack);
		EquipmentState updated = current.withPathProgress(path, progress);
		setState(stack, updated);
		return updated;
	}

	/** Restores a data-driven partial amount and consumes the field material atomically. */
	public static EquipmentOperationResult fieldRepair(ItemStack equipment, ItemStack material) {
		EquipmentState current = stateOrFailureState(equipment);
		if (!isSupported(equipment) || current == null) {
			return EquipmentOperationResult.failure(current, INVALID_ITEM);
		}
		Optional<EquipmentBalance> maybeBalance = EquipmentBalanceRegistry.current();
		if (maybeBalance.isEmpty()) {
			return EquipmentOperationResult.failure(current, BALANCE_UNAVAILABLE);
		}
		EquipmentBalance balance = maybeBalance.get();
		if (!isValidAgainstBalance(current, balance)) {
			return EquipmentOperationResult.failure(current, INVALID_STATE);
		}
		if (current.currentCondition() >= current.maxCondition()) {
			return EquipmentOperationResult.failure(current, ALREADY_FULL);
		}
		if (!canPay(equipment, material, balance.fieldRepairCost())) {
			return EquipmentOperationResult.failure(current, MISSING_MATERIAL);
		}
		int amount = EquipmentMath.fieldRepairAmount(current, balance);
		EquipmentState repaired = current.withCondition(current.currentCondition() + amount);
		commit(equipment, material, balance.fieldRepairCost().count(), repaired);
		return EquipmentOperationResult.success(repaired, FIELD_REPAIR_COMPLETE);
	}

	/** Restores full condition only when the caller proves a valid workshop is available. */
	public static EquipmentOperationResult workshopRepair(
			ItemStack equipment,
			ItemStack material,
			boolean workshopAvailable) {
		EquipmentState current = stateOrFailureState(equipment);
		if (!isSupported(equipment) || current == null) {
			return EquipmentOperationResult.failure(current, INVALID_ITEM);
		}
		if (!workshopAvailable) {
			return EquipmentOperationResult.failure(current, WORKSHOP_REQUIRED);
		}
		Optional<EquipmentBalance> maybeBalance = EquipmentBalanceRegistry.current();
		if (maybeBalance.isEmpty()) {
			return EquipmentOperationResult.failure(current, BALANCE_UNAVAILABLE);
		}
		EquipmentBalance balance = maybeBalance.get();
		if (!isValidAgainstBalance(current, balance)) {
			return EquipmentOperationResult.failure(current, INVALID_STATE);
		}
		if (current.currentCondition() >= current.maxCondition()) {
			return EquipmentOperationResult.failure(current, ALREADY_FULL);
		}
		if (!canPay(equipment, material, balance.workshopRepairCost())) {
			return EquipmentOperationResult.failure(current, MISSING_MATERIAL);
		}
		EquipmentState repaired = current.withCondition(current.maxCondition());
		commit(equipment, material, balance.workshopRepairCost().count(), repaired);
		return EquipmentOperationResult.success(repaired, WORKSHOP_REPAIR_COMPLETE);
	}

	public static EquipmentOperationResult workshopRepair(ItemStack equipment, ItemStack material) {
		return workshopRepair(equipment, material, true);
	}

	/** Switches a retained path, restores full condition, and consumes the reforge material atomically. */
	public static EquipmentOperationResult reforge(
			ItemStack equipment,
			Identifier targetPath,
			ItemStack material,
			boolean workshopAvailable) {
		EquipmentState current = stateOrFailureState(equipment);
		if (!isSupported(equipment) || current == null) {
			return EquipmentOperationResult.failure(current, INVALID_ITEM);
		}
		if (!workshopAvailable) {
			return EquipmentOperationResult.failure(current, WORKSHOP_REQUIRED);
		}
		if (targetPath == null) {
			return EquipmentOperationResult.failure(current, UNKNOWN_PATH);
		}
		Optional<EquipmentBalance> maybeBalance = EquipmentBalanceRegistry.current();
		if (maybeBalance.isEmpty()) {
			return EquipmentOperationResult.failure(current, BALANCE_UNAVAILABLE);
		}
		EquipmentBalance balance = maybeBalance.get();
		if (!isValidAgainstBalance(current, balance)) {
			return EquipmentOperationResult.failure(current, INVALID_STATE);
		}
		if (balance.path(targetPath).isEmpty() || !current.knowsPath(targetPath)) {
			return EquipmentOperationResult.failure(current, UNKNOWN_PATH);
		}
		if (current.activePath().filter(targetPath::equals).isPresent()) {
			return EquipmentOperationResult.failure(current, SAME_PATH);
		}
		if (!canPay(equipment, material, balance.reforgeCost())) {
			return EquipmentOperationResult.failure(current, MISSING_MATERIAL);
		}
		EquipmentState reforged = current.reforgedTo(targetPath);
		commit(equipment, material, balance.reforgeCost().count(), reforged);
		return EquipmentOperationResult.success(reforged, REFORGE_COMPLETE);
	}

	public static EquipmentOperationResult reforge(
			ItemStack equipment,
			Identifier targetPath,
			ItemStack material) {
		return reforge(equipment, targetPath, material, true);
	}

	private static EquipmentState ensureState(ItemStack stack) {
		requireSupported(stack);
		EquipmentState current = stack.get(EquipmentComponents.STATE);
		return current != null ? current : initializeStack(stack);
	}

	private static void requireSupported(ItemStack stack) {
		if (!isSupported(stack)) {
			throw new IllegalArgumentException("equipment operation requires a non-empty Forever equipment stack");
		}
	}

	private static void requireValidAgainstBalance(EquipmentState state, EquipmentBalance balance) {
		if (!isValidAgainstBalance(state, balance)) {
			throw new IllegalStateException("equipment state for " + state.identity()
					+ " is invalid for the active balance definitions");
		}
	}

	private static boolean isValidAgainstBalance(EquipmentState state, EquipmentBalance balance) {
		if (balance.craftsmanshipMultiplier(state.craftsmanshipTier()).isEmpty()) {
			LOGGER.error("Equipment {} refers to undefined craftsmanship tier {}", state.identity(),
					state.craftsmanshipTier());
			return false;
		}
		for (Map.Entry<Identifier, Integer> entry : state.pathProgress().entrySet()) {
			Optional<EquipmentBalance.PathDefinition> definition = balance.path(entry.getKey());
			if (definition.isPresent() && entry.getValue() > definition.get().maxProgress()) {
				LOGGER.error("Equipment {} has progress {} beyond max {} for path {}", state.identity(),
						entry.getValue(), definition.get().maxProgress(), entry.getKey());
				return false;
			}
		}
		// An absent path definition is retained as legacy_unavailable. Its effect is disabled by
		// EquipmentMath, but repair and reforge remain available so the item can recover safely.
		return true;
	}

	private static boolean canPay(ItemStack equipment, ItemStack material, EquipmentBalance.ItemCost cost) {
		if (material == null || material.isEmpty() || material == equipment || material.getCount() < cost.count()) {
			return false;
		}
		Identifier materialId = BuiltInRegistries.ITEM.getKey(material.getItem());
		return cost.item().equals(materialId);
	}

	private static void commit(ItemStack equipment, ItemStack material, int materialCount, EquipmentState state) {
		setState(equipment, state);
		material.shrink(materialCount);
	}

	private static void syncVanillaDamage(ItemStack stack, EquipmentState state) {
		if (stack.getMaxDamage() > 0) {
			stack.setDamageValue(state.vanillaDamage(stack.getMaxDamage()));
		}
	}

	private static EquipmentState stateOrFailureState(ItemStack stack) {
		return stack == null ? null : stack.get(EquipmentComponents.STATE);
	}
}
