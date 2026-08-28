package dev.forever.core.economy.domain;

/** Outcome categories used by the atomic purse transaction API. */
public enum PurseTransactionStatus {
	APPLIED,
	REJECTED,
	REPLAYED,
	OUT_OF_ORDER;
}
