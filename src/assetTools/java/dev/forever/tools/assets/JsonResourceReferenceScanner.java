package dev.forever.tools.assets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/** Parses the JSON surface needed to identify texture and model references. */
final class JsonResourceReferenceScanner {

	List<ResourceReference> scan(AssetFile file) throws IOException, UsageException {
		String source = Files.readString(file.path(), StandardCharsets.UTF_8);
		String path = file.relativePath().toString()
				.replace(file.relativePath().getFileSystem().getSeparator(), "/");
		return new Parser(path, source).parse();
	}

	private static final class Parser {
		private final String sourcePath;
		private final String input;
		private final List<ResourceReference> references = new ArrayList<>();
		private int position;

		private Parser(String sourcePath, String input) {
			this.sourcePath = sourcePath;
			this.input = input;
		}

		private List<ResourceReference> parse() throws UsageException {
			parseValue(null, false);
			skipWhitespace();
			if (position != input.length()) {
				throw error("unexpected content after the root JSON value");
			}
			return List.copyOf(references);
		}

		private void parseValue(String property, boolean textureContext) throws UsageException {
			skipWhitespace();
			if (position >= input.length()) {
				throw error("expected a JSON value");
			}
			switch (input.charAt(position)) {
				case '{' -> parseObject(textureContext);
				case '[' -> parseArray(textureContext);
				case '"' -> addReference(property, textureContext, parseString());
				case 't' -> parseLiteral("true");
				case 'f' -> parseLiteral("false");
				case 'n' -> parseLiteral("null");
				default -> parseNumber();
			}
		}

		private void parseObject(boolean parentTextureContext) throws UsageException {
			expect('{');
			skipWhitespace();
			if (consume('}')) {
				return;
			}
			while (true) {
				skipWhitespace();
				if (position >= input.length() || input.charAt(position) != '"') {
					throw error("object keys must be quoted strings");
				}
				String property = parseString();
				skipWhitespace();
				expect(':');
				parseValue(property, parentTextureContext || "textures".equals(property));
				skipWhitespace();
				if (consume('}')) {
					return;
				}
				expect(',');
			}
		}

		private void parseArray(boolean textureContext) throws UsageException {
			expect('[');
			skipWhitespace();
			if (consume(']')) {
				return;
			}
			while (true) {
				parseValue(null, textureContext);
				skipWhitespace();
				if (consume(']')) {
					return;
				}
				expect(',');
			}
		}

		private String parseString() throws UsageException {
			expect('"');
			StringBuilder value = new StringBuilder();
			while (position < input.length()) {
				char character = input.charAt(position++);
				if (character == '"') {
					return value.toString();
				}
				if (character < 0x20) {
					throw error("control characters must be escaped inside strings");
				}
				if (character != '\\') {
					value.append(character);
					continue;
				}
				if (position >= input.length()) {
					throw error("string ends after an escape character");
				}
				char escaped = input.charAt(position++);
				switch (escaped) {
					case '"', '\\', '/' -> value.append(escaped);
					case 'b' -> value.append('\b');
					case 'f' -> value.append('\f');
					case 'n' -> value.append('\n');
					case 'r' -> value.append('\r');
					case 't' -> value.append('\t');
					case 'u' -> value.append(parseUnicodeEscape());
					default -> throw error("unknown string escape \\" + escaped + "'");
				}
			}
			throw error("string is not closed");
		}

		private char parseUnicodeEscape() throws UsageException {
			if (position + 4 > input.length()) {
				throw error("unicode escape is incomplete");
			}
			int value = 0;
			for (int index = 0; index < 4; index++) {
				int digit = Character.digit(input.charAt(position++), 16);
				if (digit < 0) {
					throw error("unicode escape contains a non-hexadecimal digit");
				}
				value = value * 16 + digit;
			}
			return (char) value;
		}

		private void parseLiteral(String literal) throws UsageException {
			if (!input.startsWith(literal, position)) {
				throw error("invalid JSON literal");
			}
			position += literal.length();
		}

		private void parseNumber() throws UsageException {
			int start = position;
			consume('-');
			if (consume('0')) {
				// The delimiter checks below reject a second leading digit.
			} else {
				consumeDigits();
			}
			if (position == start || (position == start + 1 && input.charAt(start) == '-')) {
				throw error("invalid JSON number");
			}
			if (consume('.')) {
				int fractionStart = position;
				consumeDigits();
				if (position == fractionStart) {
					throw error("number fraction is missing digits");
				}
			}
			if (consume('e') || consume('E')) {
				consume('+');
				consume('-');
				int exponentStart = position;
				consumeDigits();
				if (position == exponentStart) {
					throw error("number exponent is missing digits");
				}
			}
			if (position < input.length() && Character.isLetter(input.charAt(position))) {
				throw error("invalid character after JSON number");
			}
		}

		private void consumeDigits() {
			while (position < input.length() && Character.isDigit(input.charAt(position))) {
				position++;
			}
		}

		private void addReference(String property, boolean textureContext, String value) {
			if (!value.startsWith("forever:")) {
				return;
			}
			String resourcePath = value.substring("forever:".length());
			ReferenceKind kind = isModelProperty(property)
					? ReferenceKind.MODEL
					: (textureContext || isTextureProperty(property) ? ReferenceKind.TEXTURE : ReferenceKind.UNKNOWN);
			references.add(new ResourceReference(sourcePath, property == null ? "value" : property, resourcePath, kind));
		}

		private static boolean isModelProperty(String property) {
			return "model".equals(property) || "parent".equals(property);
		}

		private static boolean isTextureProperty(String property) {
			return "texture".equals(property) || "particle".equals(property)
					|| (property != null && property.startsWith("layer"));
		}

		private void skipWhitespace() {
			while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
				position++;
			}
		}

		private void expect(char expected) throws UsageException {
			if (!consume(expected)) {
				throw error("expected '" + expected + "'");
			}
		}

		private boolean consume(char expected) {
			if (position < input.length() && input.charAt(position) == expected) {
				position++;
				return true;
			}
			return false;
		}

		private UsageException error(String message) {
			return new UsageException("JSON " + sourcePath + " is invalid at character " + position + ": " + message + ".");
		}
	}
}
