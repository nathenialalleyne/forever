package dev.forever.core.economy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Atomic Coin Purse conversion service. The sequence is assigned by the server and persisted
 * with the balance, so an old or replayed request cannot be applied after an interleaved update.
 */
public final class PurseService {

	private static final int MAX_CONTAINER_SLOTS = 256;

	private PurseService() {
	}

	/** Applies a logical deposit after the caller has reserved the physical coins. */
	public static PurseTransactionResult deposit(
			CoinPurseState state, long transactionSequence, long amount, EconomyBalance balance) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(balance, "balance");
		PurseTransactionResult sequenceResult = validateSequence(state, transactionSequence);
		if (sequenceResult != null) {
			return sequenceResult;
		}
		if (amount < 1L) {
			return rejected(state, "A purse deposit must be positive.");
		}
		if (amount > balance.maxTransaction()) {
			return rejected(state, "The purse deposit exceeds the data-defined transaction limit.");
		}
		if (amount % balance.obolValue() != 0L) {
			return rejected(state, "The purse deposit must contain complete physical Obol denominations.");
		}
		long nextBalance;
		try {
			nextBalance = Math.addExact(state.balance(), amount);
		} catch (ArithmeticException exception) {
			return rejected(state, "The purse deposit would overflow the balance range.");
		}
		if (nextBalance > balance.maxPurseBalance()) {
			return rejected(state, "The purse deposit would exceed the data-defined purse limit.");
		}
		CoinPurseState next = state.withBalanceAndSequence(nextBalance, transactionSequence);
		return new PurseTransactionResult(true, PurseTransactionStatus.APPLIED, next, amount, -amount,
				"The physical Obols were deposited into the Coin Purse.");
	}

	/** Applies a logical withdrawal before the caller releases physical coins. */
	public static PurseTransactionResult withdraw(
			CoinPurseState state, long transactionSequence, long amount, EconomyBalance balance) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(balance, "balance");
		PurseTransactionResult sequenceResult = validateSequence(state, transactionSequence);
		if (sequenceResult != null) {
			return sequenceResult;
		}
		if (amount < 1L) {
			return rejected(state, "A purse withdrawal must be positive.");
		}
		if (amount > balance.maxTransaction()) {
			return rejected(state, "The purse withdrawal exceeds the data-defined transaction limit.");
		}
		if (amount % balance.obolValue() != 0L) {
			return rejected(state, "The purse withdrawal must produce complete physical Obol denominations.");
		}
		if (state.balance() < amount) {
			return rejected(state, "The Coin Purse does not contain enough balance.");
		}
		CoinPurseState next = state.withBalanceAndSequence(state.balance() - amount, transactionSequence);
		return new PurseTransactionResult(true, PurseTransactionStatus.APPLIED, next, -amount, amount,
				"The Coin Purse balance was withdrawn as physical Obols.");
	}

	/** Reads the persistent player attachment, creating only its safe empty default. */
	public static CoinPurseState stateOf(Player player) {
		Objects.requireNonNull(player, "player");
		return ((AttachmentTarget) player).getAttachedOrCreate(CoinPurseAttachment.TYPE);
	}

	public static long balanceOf(Player player) {
		return stateOf(player).balance();
	}

	/** Deposits exactly amount from the player's ordinary inventory. */
	public static PurseTransactionResult deposit(Player player, long amount) {
		Objects.requireNonNull(player, "player");
		EconomyBalance balance = EconomyBalanceAccess.requireCurrent();
		synchronized (player) {
			CoinPurseState oldState = stateOf(player);
			long sequence = nextSequenceOrReject(oldState);
			if (sequence < 0L) {
				return rejected(oldState, "The Coin Purse transaction sequence is exhausted.");
			}
			if (amount < 1L || amount % balance.obolValue() != 0L) {
				return rejected(oldState, "The requested deposit is not a positive complete Obol amount.");
			}
			long coins = amount / balance.obolValue();
			Container inventory = player.getInventory();
			if (physicalValue(inventory, balance) < amount) {
				return rejected(oldState, "The player's inventory does not contain the requested physical Obols.");
			}
			List<ItemStack> original = snapshot(inventory);
			List<ItemStack> simulated = copy(original);
			if (!removeObols(simulated, coins)) {
				return rejected(oldState, "The physical Obol reservation failed; no inventory was changed.");
			}
			PurseTransactionResult logical = deposit(oldState, sequence, amount, balance);
			if (!logical.applied()) {
				return logical;
			}
			try {
				commit(inventory, simulated);
				setState(player, logical.state());
			} catch (RuntimeException exception) {
				try {
					commit(inventory, original);
				} catch (RuntimeException restoreException) {
					exception.addSuppressed(restoreException);
				}
				try {
					setState(player, oldState);
				} catch (RuntimeException restoreException) {
					exception.addSuppressed(restoreException);
				}
				throw new IllegalStateException("Coin Purse deposit could not commit its attachment revision.", exception);
			}
			return logical;
		}
	}

	/** Withdraws exactly amount into the player's ordinary inventory, failing closed if it will not fit. */
	public static PurseTransactionResult withdraw(Player player, long amount) {
		Objects.requireNonNull(player, "player");
		EconomyBalance balance = EconomyBalanceAccess.requireCurrent();
		synchronized (player) {
			CoinPurseState oldState = stateOf(player);
			long sequence = nextSequenceOrReject(oldState);
			if (sequence < 0L) {
				return rejected(oldState, "The Coin Purse transaction sequence is exhausted.");
			}
			if (amount < 1L || amount % balance.obolValue() != 0L) {
				return rejected(oldState, "The requested withdrawal is not a positive complete Obol amount.");
			}
			long coins = amount / balance.obolValue();
			Container inventory = player.getInventory();
			List<ItemStack> original = snapshot(inventory);
			List<ItemStack> simulated = copy(original);
			if (!insertObols(simulated, coins, balance)) {
				return rejected(oldState, "The player's inventory cannot fit the physical Obols; no balance was changed.");
			}
			PurseTransactionResult logical = withdraw(oldState, sequence, amount, balance);
			if (!logical.applied()) {
				return logical;
			}
			try {
				commit(inventory, simulated);
				setState(player, logical.state());
			} catch (RuntimeException exception) {
				try {
					commit(inventory, original);
				} catch (RuntimeException restoreException) {
					exception.addSuppressed(restoreException);
				}
				try {
					setState(player, oldState);
				} catch (RuntimeException restoreException) {
					exception.addSuppressed(restoreException);
				}
				throw new IllegalStateException("Coin Purse withdrawal could not commit its attachment revision.", exception);
			}
			return logical;
		}
	}

	/** Returns physical Obol value in a bounded container without mutating it. */
	public static long physicalValue(Container container, EconomyBalance balance) {
		Objects.requireNonNull(container, "container");
		Objects.requireNonNull(balance, "balance");
		if (container.getContainerSize() < 0 || container.getContainerSize() > MAX_CONTAINER_SLOTS) {
			throw new IllegalArgumentException("The physical Obol container exceeds the bounded inspection limit.");
		}
		long count = 0L;
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			ItemStack stack = container.getItem(slot);
			if (!ObolItem.isObol(stack)) {
				continue;
			}
			try {
				count = Math.addExact(count, stack.getCount());
			} catch (ArithmeticException exception) {
				return Long.MAX_VALUE;
			}
		}
		try {
			return Math.multiplyExact(count, balance.obolValue());
		} catch (ArithmeticException exception) {
			return Long.MAX_VALUE;
		}
	}

	private static PurseTransactionResult validateSequence(CoinPurseState state, long sequence) {
		if (sequence <= state.lastTransactionSequence()) {
			return new PurseTransactionResult(false, PurseTransactionStatus.REPLAYED, state, 0L, 0L,
					"The purse request was already applied or is older than the current revision.");
		}
		if (state.lastTransactionSequence() == Long.MAX_VALUE
				|| sequence != state.lastTransactionSequence() + 1L) {
			return new PurseTransactionResult(false, PurseTransactionStatus.OUT_OF_ORDER, state, 0L, 0L,
					"The purse request sequence is not the next server-authorised revision.");
		}
		return null;
	}

	private static long nextSequenceOrReject(CoinPurseState state) {
		return state.lastTransactionSequence() == Long.MAX_VALUE ? -1L : state.lastTransactionSequence() + 1L;
	}

	private static List<ItemStack> snapshot(Container container) {
		if (container.getContainerSize() < 0 || container.getContainerSize() > MAX_CONTAINER_SLOTS) {
			throw new IllegalArgumentException("The physical Obol container exceeds the bounded inspection limit.");
		}
		List<ItemStack> result = new ArrayList<>(container.getContainerSize());
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			ItemStack stack = container.getItem(slot);
			result.add(stack == null ? ItemStack.EMPTY : stack.copy());
		}
		return result;
	}

	private static List<ItemStack> copy(List<ItemStack> contents) {
		List<ItemStack> copy = new ArrayList<>(contents.size());
		for (ItemStack stack : contents) {
			copy.add(stack.copy());
		}
		return copy;
	}

	private static void commit(Container container, List<ItemStack> contents) {
		for (int slot = 0; slot < contents.size(); slot++) {
			container.setItem(slot, contents.get(slot));
		}
		container.setChanged();
	}

	private static boolean removeObols(List<ItemStack> contents, long amount) {
		long remaining = amount;
		for (int slot = 0; slot < contents.size() && remaining > 0L; slot++) {
			ItemStack stack = contents.get(slot);
			if (!ObolItem.isObol(stack)) {
				continue;
			}
			int remove = (int) Math.min((long) stack.getCount(), remaining);
			stack.shrink(remove);
			if (stack.isEmpty()) {
				contents.set(slot, ItemStack.EMPTY);
			}
			remaining -= remove;
		}
		return remaining == 0L;
	}

	private static boolean insertObols(List<ItemStack> contents, long amount, EconomyBalance balance) {
		long remaining = amount;
		ItemStack prototype = new ItemStack(ObolItem.ITEM);
		int maxStack = Math.min(prototype.getMaxStackSize(), balance.physicalStackLimit());
		if (maxStack < 1) {
			return false;
		}
		for (int slot = 0; slot < contents.size() && remaining > 0L; slot++) {
			ItemStack stack = contents.get(slot);
			if (!ObolItem.isObol(stack) || !ItemStack.isSameItemSameComponents(stack, prototype)) {
				continue;
			}
			int capacity = Math.max(0, maxStack - stack.getCount());
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

	private static void setState(Player player, CoinPurseState state) {
		((AttachmentTarget) player).setAttached(CoinPurseAttachment.TYPE, state);
	}

	private static PurseTransactionResult rejected(CoinPurseState state, String message) {
		return new PurseTransactionResult(false, PurseTransactionStatus.REJECTED, state, 0L, 0L, message);
	}
}
