package dev.forever.core.career;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative preview and atomic inventory operation for Mason bulk supply. */
public final class MasonSupplyService {

	private static final int MAX_CONTAINER_SLOTS = 256;

	private MasonSupplyService() {
	}

	public static MasonSupplyResult preview(
			CareerState state, MasonCatalog catalog, Identifier entryId, int batchCount) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(catalog, "catalog");
		Objects.requireNonNull(entryId, "entryId");
		MasonCatalogEntry entry = catalog.entry(entryId).orElse(null);
		if (entry == null) {
			return rejected(null, batchCount, "The requested Mason catalogue entry does not exist.");
		}
		if (!state.isMason() || state.status() != CareerStatus.ACTIVE) {
			return rejected(entry, batchCount, "Only an active Mason may use the catalogue.");
		}
		if (!state.rank().atLeast(entry.minimumRank())) {
			return rejected(entry, batchCount, "The Mason rank is below the entry's minimum rank.");
		}
		if (batchCount < 1 || batchCount > entry.maxBatch()) {
			return rejected(entry, batchCount, "The requested batch is outside the data-defined bound of "
					+ entry.maxBatch() + ".");
		}
		try {
			long input = entry.requiredInput(batchCount);
			long output = entry.producedOutput(batchCount);
			return new MasonSupplyResult(true, entry, batchCount, input, output,
					"The Mason catalogue preview is valid and bounded.");
		} catch (ArithmeticException exception) {
			return rejected(entry, batchCount, "The requested catalogue batch overflows the safe count range.");
		}
	}

	/** Preview that also verifies the entry capability exposed by the current rank data. */
	public static MasonSupplyResult preview(
			CareerState state, MasonData data, Identifier entryId, int batchCount) {
		Objects.requireNonNull(data, "data");
		MasonSupplyResult result = preview(state, data.catalog(), entryId, batchCount);
		if (!result.applied()) {
			return result;
		}
		MasonRankRule rule = data.career().ruleFor(state.rank()).orElse(null);
		if (rule == null || !rule.capabilities().contains(result.entry().capability())) {
			return rejected(result.entry(), batchCount,
					"The current Mason rank does not expose this catalogue capability.");
		}
		return result;
	}

	/**
	 * Consumes input and inserts output as one simulated revision. If either side cannot be
	 * completed, neither container is changed.
	 */
	public static MasonSupplyResult supply(
			CareerState state,
			MasonCatalog catalog,
			Identifier entryId,
			int batchCount,
			Container input,
			Container output) {
		Objects.requireNonNull(input, "input");
		Objects.requireNonNull(output, "output");
		MasonSupplyResult plan = preview(state, catalog, entryId, batchCount);
		if (!plan.applied()) {
			return plan;
		}
		if (!bounded(input) || !bounded(output)) {
			return rejected(plan.entry(), batchCount, "Mason supply containers exceed the bounded slot limit.");
		}
		if (!BuiltInRegistries.ITEM.containsKey(plan.entry().input())
				|| !BuiltInRegistries.ITEM.containsKey(plan.entry().output())) {
			return rejected(plan.entry(), batchCount, "The catalogue references an unknown item and is disabled.");
		}
		Item inputItem = BuiltInRegistries.ITEM.getOptional(plan.entry().input()).orElseThrow();
		Item outputItem = BuiltInRegistries.ITEM.getOptional(plan.entry().output()).orElseThrow();

		if (input == output) {
			List<ItemStack> simulated = snapshot(input);
			if (!remove(simulated, inputItem, plan.inputConsumed())
					|| !insert(simulated, outputItem, plan.outputProduced())) {
				return rejected(plan.entry(), batchCount,
						"The Mason supply could not fit its output after reserving the input.");
			}
			List<ItemStack> original = snapshot(input);
			try {
				commit(input, simulated);
			} catch (RuntimeException exception) {
				try {
					commit(input, original);
				} catch (RuntimeException restoreException) {
					exception.addSuppressed(restoreException);
				}
				throw new IllegalStateException("Mason supply could not commit its single-container revision.", exception);
			}
		} else {
			List<ItemStack> inputSnapshot = snapshot(input);
			List<ItemStack> outputSnapshot = snapshot(output);
			List<ItemStack> originalInput = snapshot(input);
			List<ItemStack> originalOutput = snapshot(output);
			if (!remove(inputSnapshot, inputItem, plan.inputConsumed())
					|| !insert(outputSnapshot, outputItem, plan.outputProduced())) {
				return rejected(plan.entry(), batchCount,
						"The Mason supply could not reserve input and output atomically.");
			}
			try {
				commit(input, inputSnapshot);
				commit(output, outputSnapshot);
			} catch (RuntimeException exception) {
				try {
					commit(input, originalInput);
				} catch (RuntimeException restoreException) {
					exception.addSuppressed(restoreException);
				}
				try {
					commit(output, originalOutput);
				} catch (RuntimeException restoreException) {
					exception.addSuppressed(restoreException);
				}
				throw new IllegalStateException("Mason supply could not commit both container revisions.", exception);
			}
		}
		return new MasonSupplyResult(true, plan.entry(), batchCount, plan.inputConsumed(), plan.outputProduced(),
				"The Mason bulk supply committed without deleting or duplicating items.");
	}

	public static MasonSupplyResult supply(
			CareerState state,
			MasonData data,
			Identifier entryId,
			int batchCount,
			Container input,
			Container output) {
		Objects.requireNonNull(data, "data");
		MasonSupplyResult capabilityCheck = preview(state, data, entryId, batchCount);
		if (!capabilityCheck.applied()) {
			return capabilityCheck;
		}
		return supply(state, data.catalog(), entryId, batchCount, input, output);
	}

	private static boolean bounded(Container container) {
		return container.getContainerSize() >= 0 && container.getContainerSize() <= MAX_CONTAINER_SLOTS;
	}

	private static List<ItemStack> snapshot(Container container) {
		List<ItemStack> result = new ArrayList<>(container.getContainerSize());
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			ItemStack stack = container.getItem(slot);
			result.add(stack == null ? ItemStack.EMPTY : stack.copy());
		}
		return result;
	}

	private static void commit(Container container, List<ItemStack> contents) {
		for (int slot = 0; slot < contents.size(); slot++) {
			container.setItem(slot, contents.get(slot));
		}
		container.setChanged();
	}

	private static boolean remove(List<ItemStack> contents, Item item, long amount) {
		long remaining = amount;
		for (int slot = 0; slot < contents.size() && remaining > 0L; slot++) {
			ItemStack stack = contents.get(slot);
			if (stack.isEmpty() || stack.getItem() != item) {
				continue;
			}
			int take = (int) Math.min((long) stack.getCount(), remaining);
			stack.shrink(take);
			if (stack.isEmpty()) {
				contents.set(slot, ItemStack.EMPTY);
			}
			remaining -= take;
		}
		return remaining == 0L;
	}

	private static boolean insert(List<ItemStack> contents, Item item, long amount) {
		long remaining = amount;
		ItemStack prototype = new ItemStack(item);
		int maxStack = Math.max(1, prototype.getMaxStackSize());
		for (int slot = 0; slot < contents.size() && remaining > 0L; slot++) {
			ItemStack stack = contents.get(slot);
			if (stack.isEmpty() || stack.getItem() != item || !ItemStack.isSameItemSameComponents(stack, prototype)) {
				continue;
			}
			int capacity = Math.max(0, stack.getMaxStackSize() - stack.getCount());
			int add = (int) Math.min((long) capacity, remaining);
			if (add > 0) {
				stack.grow(add);
				remaining -= add;
			}
		}
		for (int slot = 0; slot < contents.size() && remaining > 0L; slot++) {
			if (!contents.get(slot).isEmpty()) {
				continue;
			}
			int add = (int) Math.min((long) maxStack, remaining);
			contents.set(slot, prototype.copyWithCount(add));
			remaining -= add;
		}
		return remaining == 0L;
	}

	private static MasonSupplyResult rejected(MasonCatalogEntry entry, int batchCount, String message) {
		if (entry == null) {
			entry = new MasonCatalogEntry(
					Identifier.fromNamespaceAndPath("forever", "invalid"),
					Identifier.fromNamespaceAndPath("minecraft", "air"),
					1,
					Identifier.fromNamespaceAndPath("minecraft", "air"),
					1,
					1,
					MasonRank.APPRENTICE,
					Identifier.fromNamespaceAndPath("forever", "invalid"));
		}
		return new MasonSupplyResult(false, entry, Math.max(0, batchCount), 0L, 0L, message);
	}
}
