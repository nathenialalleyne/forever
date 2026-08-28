package dev.forever.tools.assets;

import java.util.ArrayList;
import java.util.List;

/** Small RFC4180 reader supporting commas, escaped quotes, and quoted newlines. */
final class Rfc4180Parser {

	private Rfc4180Parser() {
	}

	static List<List<String>> parse(String source) throws UsageException {
		if (source.startsWith("\uFEFF")) {
			source = source.substring(1);
		}
		List<List<String>> records = new ArrayList<>();
		List<String> fields = new ArrayList<>();
		StringBuilder field = new StringBuilder();
		boolean inQuotes = false;
		boolean afterClosingQuote = false;
		boolean recordStarted = false;
		for (int index = 0; index < source.length(); index++) {
			char character = source.charAt(index);
			if (inQuotes) {
				if (character == '"') {
					if (index + 1 < source.length() && source.charAt(index + 1) == '"') {
						field.append('"');
						index++;
					} else {
						inQuotes = false;
						afterClosingQuote = true;
					}
				} else {
					field.append(character);
				}
				continue;
			}
			if (afterClosingQuote) {
				if (character == ',') {
					fields.add(field.toString());
					field.setLength(0);
					afterClosingQuote = false;
				} else if (character == '\r' || character == '\n') {
					fields.add(field.toString());
					addRecord(records, fields);
					fields = new ArrayList<>();
					field.setLength(0);
					afterClosingQuote = false;
					recordStarted = false;
					if (character == '\r' && index + 1 < source.length() && source.charAt(index + 1) == '\n') {
						index++;
					}
				} else {
					throw malformed(index, "characters after a closing quote must be a comma or record ending");
				}
				continue;
			}
			switch (character) {
				case '"' -> {
					if (field.length() != 0) {
						throw malformed(index, "a quote may only start a quoted field");
					}
					inQuotes = true;
					recordStarted = true;
				}
				case ',' -> {
					fields.add(field.toString());
					field.setLength(0);
					recordStarted = true;
				}
				case '\r', '\n' -> {
					if (!recordStarted && fields.isEmpty() && field.isEmpty()) {
						throw malformed(index, "blank records are not allowed");
					}
					fields.add(field.toString());
					addRecord(records, fields);
					fields = new ArrayList<>();
					field.setLength(0);
					recordStarted = false;
					if (character == '\r' && index + 1 < source.length() && source.charAt(index + 1) == '\n') {
						index++;
					}
				}
				default -> {
					field.append(character);
					recordStarted = true;
				}
			}
		}
		if (inQuotes) {
			throw new UsageException("Manifest CSV ends inside a quoted field.");
		}
		if (afterClosingQuote || recordStarted || !fields.isEmpty() || !field.isEmpty()) {
			fields.add(field.toString());
			addRecord(records, fields);
		}
		return List.copyOf(records);
	}

	private static void addRecord(List<List<String>> records, List<String> fields) {
		records.add(List.copyOf(fields));
	}

	private static UsageException malformed(int index, String reason) {
		return new UsageException("Manifest CSV is not valid RFC4180 at character " + index + ": " + reason + ".");
	}
}
