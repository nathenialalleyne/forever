package dev.forever.core.guide.adapter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Protects the principle-13 boundary for guide-owned source and data. */
class GuideMatchaIsolationTest {

	private static final Pattern NAMESPACED_ID = Pattern.compile(
			"(?<![A-Za-z0-9_./-])[a-z][a-z0-9_-]*:[a-z0-9_./-]+(?![A-Za-z0-9_./-])");
	private static final Pattern AUDITED_DATA_PATH = Pattern.compile(
			"(?<![A-Za-z0-9_./-])(?:data|assets)/[A-Za-z0-9_./-]+\\.[A-Za-z0-9]+(?![A-Za-z0-9_./-])");
	private static final Pattern BACKTICK = Pattern.compile("`([^`]+)`");

	@Test
	void guideDataAndCodeDoNotContainAuditedPrivateIdentifiers() throws IOException {
		Path root = Path.of("").toAbsolutePath().normalize();
		Set<String> forbidden = auditedPrivateTokens(root);
		List<String> violations = new ArrayList<>();

		for (Path directory : List.of(
				root.resolve("src/main/java/dev/forever/core/guide"),
				root.resolve("src/test/java/dev/forever/core/guide"),
				root.resolve("src/gametest/java/dev/forever/gametest/guide"),
				root.resolve("src/main/resources/data/forever/guide"))) {
			if (!Files.isDirectory(directory)) {
				continue;
			}
			try (Stream<Path> paths = Files.walk(directory)) {
				paths.filter(Files::isRegularFile).forEach(path -> {
					try {
						String contents = Files.readString(path);
						for (String token : forbidden) {
							if (containsToken(contents, token)) {
								violations.add(root.relativize(path) + " contains audited private token " + token);
							}
						}
					} catch (IOException exception) {
						throw new GuideIsolationFailure("Could not scan " + path, exception);
					}
				});
			}
		}

		assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
	}

	private static Set<String> auditedPrivateTokens(Path root) throws IOException {
		String findings = Files.readString(root.resolve("docs/matcha-audit/findings.md"));
		String classifications = Files.readString(root.resolve("docs/matcha-audit/system-classification.csv"));
		Set<String> tokens = new HashSet<>();
		Matcher namespaced = NAMESPACED_ID.matcher(findings + "\n" + classifications);
		while (namespaced.find()) {
			String token = namespaced.group();
			if (!token.startsWith("forever:")) {
				tokens.add(token);
			}
		}
		Matcher paths = AUDITED_DATA_PATH.matcher(findings + "\n" + classifications);
		while (paths.find()) {
			tokens.add(paths.group());
		}
		for (String line : findings.lines().toList()) {
			if (line.contains("Scoreboards such as")) {
				Matcher backtick = BACKTICK.matcher(line);
				while (backtick.find()) {
					tokens.add(backtick.group(1));
				}
			}
		}
		return Set.copyOf(tokens);
	}

	private static boolean containsToken(String contents, String token) {
		if (token.contains(":")) {
			return Pattern.compile("(?<![A-Za-z0-9_./-])" + Pattern.quote(token)
					+ "(?![A-Za-z0-9_./-])").matcher(contents).find();
		}
		return Pattern.compile("(?<![A-Za-z0-9_./-])" + Pattern.quote(token)
				+ "(?![A-Za-z0-9_./-])").matcher(contents).find();
	}

	private static final class GuideIsolationFailure extends RuntimeException {
		private GuideIsolationFailure(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
