package dev.forever.core.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Mutable server-side view over an immutable persisted inventory-index snapshot.
 *
 * <p>It only accepts snapshots supplied by an explicit container registration. It never finds
 * containers by scanning a world and never owns physical ItemStacks as an authority.
 */
public final class InventoryIndex {

	public static final String TOO_MANY_SLOTS = "forever.storage.index_limit_reached";
	public static final String INVALID_CONTAINER = "forever.storage.index_invalid_container";

	private static final Comparator<InventoryIndexEntry> ENTRY_ORDER = Comparator
			.comparing((InventoryIndexEntry entry) -> entry.container().dimension().identifier().toString())
			.thenComparingLong(entry -> entry.container().position().asLong())
			.thenComparingInt(InventoryIndexEntry::slot)
			.thenComparing(entry -> entry.itemId().toString());

	private final StorageBalance balance;
	private InventoryIndexState state;

	public InventoryIndex(StorageBalance balance) {
		this(balance, InventoryIndexState.empty());
	}

	public InventoryIndex(StorageBalance balance, InventoryIndexState state) {
		this.balance = Objects.requireNonNull(balance, "balance");
		this.state = Objects.requireNonNull(state, "state");
		if (state.entries().size() > balance.maxIndexedSlots()) {
			throw new IllegalArgumentException("persisted storage index exceeds the active slot limit");
		}
	}

	public InventoryIndexState state() {
		return state;
	}

	/** Replaces one explicitly registered container's cached non-empty slots. */
	public IndexMutationResult add(ContainerReference reference, List<ItemStack> slots) {
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(slots, "slots");
		List<InventoryIndexEntry> replacement = entriesFor(reference, slots);
		int existing = (int) state.entries().stream()
				.filter(entry -> !entry.container().equals(reference))
				.count();
		if (existing + replacement.size() > balance.maxIndexedSlots()) {
			return IndexMutationResult.rejected(TOO_MANY_SLOTS, existing);
		}

		List<InventoryIndexEntry> nextEntries = new ArrayList<>();
		for (InventoryIndexEntry entry : state.entries()) {
			if (!entry.container().equals(reference)) {
				nextEntries.add(entry);
			}
		}
		nextEntries.addAll(replacement);
		List<ContainerReference> nextInvalidated = state.invalidated().stream()
				.filter(value -> !value.equals(reference))
				.toList();
		state = state.withEntries(nextEntries, nextInvalidated);
		return IndexMutationResult.success(replacement.size());
	}

	/** Removes all cached data for a container that is no longer registered. */
	public IndexMutationResult remove(ContainerReference reference) {
		Objects.requireNonNull(reference, "reference");
		int previous = state.entries().size();
		List<InventoryIndexEntry> nextEntries = state.entries().stream()
				.filter(entry -> !entry.container().equals(reference))
				.toList();
		List<ContainerReference> nextInvalidated = state.invalidated().stream()
				.filter(value -> !value.equals(reference))
				.toList();
		state = state.withEntries(nextEntries, nextInvalidated);
		return IndexMutationResult.success(previous - nextEntries.size());
	}

	/** Marks cached data stale until the explicitly registered container is refreshed. */
	public IndexMutationResult invalidate(ContainerReference reference) {
		Objects.requireNonNull(reference, "reference");
		if (state.invalidated().contains(reference)) {
			return IndexMutationResult.success(indexedSlots(reference));
		}
		List<ContainerReference> nextInvalidated = new ArrayList<>(state.invalidated());
		nextInvalidated.add(reference);
		state = state.withEntries(state.entries(), nextInvalidated);
		return IndexMutationResult.success(indexedSlots(reference));
	}

	/** Returns a bounded projection of the cached entries. */
	public WarehouseSearchPage query(WarehouseSearchRequest request) {
		Objects.requireNonNull(request, "request");
		if (request.validateFor(balance).error().isPresent()) {
			return WarehouseSearchPage.rejected("forever.storage.search_request_rejected");
		}
		String text = request.text().toLowerCase(Locale.ROOT);
		List<InventoryIndexEntry> matching = state.entries().stream()
				.filter(entry -> matches(entry, text, request.facets()))
				.sorted(ENTRY_ORDER)
				.toList();
		int from = Math.min(request.offset(), matching.size());
		int to = Math.min(from + request.limit(), matching.size());
		List<WarehouseSearchResult> results = new ArrayList<>(to - from);
		for (InventoryIndexEntry entry : matching.subList(from, to)) {
			boolean stale = state.invalidated().contains(entry.container()) || !entry.available();
			results.add(new WarehouseSearchResult(
					entry.container(),
					entry.slot(),
					entry.itemId(),
					entry.quantity(),
					entry.damage(),
					entry.maxDamage(),
					stale ? false : entry.available(),
					stale,
					stale ? "stale" : "registered",
					stale ? "forever.storage.index_stale" : "",
					entry.fingerprint()));
		}
		return new WarehouseSearchPage(
				WarehouseSearchRequest.CURRENT_PROTOCOL,
				results,
				to < matching.size(),
				matching.size(),
				"ok",
				"");
	}

	private List<InventoryIndexEntry> entriesFor(ContainerReference reference, List<ItemStack> slots) {
		if (slots.size() > balance.maxIndexedSlots()) {
			throw new IllegalArgumentException(INVALID_CONTAINER);
		}
		long revision = revisionOf(slots);
		List<InventoryIndexEntry> entries = new ArrayList<>();
		for (int slot = 0; slot < slots.size(); slot++) {
			ItemStack stack = Objects.requireNonNull(slots.get(slot), "slots cannot contain null");
			if (stack.isEmpty()) {
				continue;
			}
			Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
			if (itemId == null) {
				throw new IllegalArgumentException("cannot index an item without a registry identifier");
			}
			entries.add(new InventoryIndexEntry(
					reference,
				slot,
				itemId,
				stack.getCount(),
				stack.isDamageableItem() ? stack.getDamageValue() : 0,
				stack.isDamageableItem() ? stack.getMaxDamage() : 0,
				ItemStack.hashItemAndComponents(stack),
				revision,
				true));
		}
		return entries;
	}

	private int indexedSlots(ContainerReference reference) {
		return (int) state.entries().stream().filter(entry -> entry.container().equals(reference)).count();
	}

	private static boolean matches(InventoryIndexEntry entry, String text, List<String> facets) {
		String id = entry.itemId().toString().toLowerCase(Locale.ROOT);
		if (!text.isBlank() && !id.contains(text)) {
			return false;
		}
		for (String facet : facets) {
			if (!facet.isBlank() && !id.contains(facet)) {
				return false;
			}
		}
		return true;
	}

	private static long revisionOf(List<ItemStack> slots) {
		long revision = 1L;
		for (int index = 0; index < slots.size(); index++) {
			ItemStack stack = Objects.requireNonNull(slots.get(index), "slots cannot contain null");
			revision = revision * 31L + index;
			revision = revision * 31L + ItemStack.hashItemAndComponents(stack);
			revision = revision * 31L + stack.getCount();
			revision = revision * 31L + (stack.isDamageableItem() ? stack.getDamageValue() : 0);
		}
		return revision & Long.MAX_VALUE;
	}
}
