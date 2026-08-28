package dev.forever.tools.assets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Rejects filename stems and directories that cannot be stable asset identifiers. */
final class NameNotSnakeCaseCheck implements AssetRule {

	private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9_]*$");

	@Override
	public ViolationCode code() {
		return ViolationCode.NAME_NOT_SNAKE_CASE;
	}

	@Override
	public List<AssetViolation> check(AssetValidationContext context) {
		List<AssetViolation> violations = new ArrayList<>();
		for (Path directory : context.inventory().directories()) {
			String name = directory.getFileName().toString();
			if (!SNAKE_CASE.matcher(name).matches()) {
				violations.add(new AssetViolation(code(), pathName(directory),
						"Directory name '" + name + "' must match ^[a-z][a-z0-9_]*$."));
			}
		}
		// The contract covers resource filename stems. Marker files such as .gitkeep
		// support empty planned directories and are not production resources.
		for (AssetFile file : context.inventory().files()) {
			if (".gitkeep".equals(file.relativePath().getFileName().toString())) {
				continue;
			}
			if (!SNAKE_CASE.matcher(file.stem()).matches()) {
				violations.add(new AssetViolation(code(), pathName(file.relativePath()),
						"Filename stem '" + file.stem() + "' must match ^[a-z][a-z0-9_]*$."));
			}
		}
		return violations;
	}

	private static String pathName(Path path) {
		return path.toString().replace(path.getFileSystem().getSeparator(), "/");
	}
}
