package dev.forever.tools.assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Verifies Forever texture and model references in the scoped JSON directories. */
final class MissingTextureReferenceCheck implements AssetRule {

	private final JsonResourceReferenceScanner scanner = new JsonResourceReferenceScanner();
	private final ResourceReferenceResolver resolver = new ResourceReferenceResolver();

	@Override
	public ViolationCode code() {
		return ViolationCode.MISSING_TEXTURE_REFERENCE;
	}

	@Override
	public List<AssetViolation> check(AssetValidationContext context) throws IOException, UsageException {
		Map<String, ResourceReference> references = new LinkedHashMap<>();
		for (AssetFile file : context.inventory().referenceJsons()) {
			for (ResourceReference reference : scanner.scan(file)) {
				String key = reference.sourcePath() + "|" + reference.kind() + "|" + reference.resourcePath();
				references.putIfAbsent(key, reference);
			}
		}
		List<ResourceReference> ordered = references.values().stream()
				.sorted(Comparator.comparing(ResourceReference::sourcePath)
						.thenComparing(ResourceReference::resourcePath)
						.thenComparing(reference -> reference.kind().name())
						.thenComparing(ResourceReference::property))
				.toList();
		List<AssetViolation> violations = new ArrayList<>();
		for (ResourceReference reference : ordered) {
			ResourceReferenceResolver.ResolutionResult resolution = resolver.resolve(
					context.assetsDirectory(), reference);
			if (!resolution.valid()) {
				violations.add(new AssetViolation(code(), reference.sourcePath(),
						"Reference forever:" + reference.resourcePath() + " is invalid: " + resolution.invalidReason() + "."));
				continue;
			}
			if (resolution.candidates().stream().noneMatch(Files::isRegularFile)) {
				String expected = resolution.candidates().stream()
						.map(path -> relativeAssetPath(context.assetsDirectory(), path))
						.sorted()
						.reduce((left, right) -> left + " or " + right)
						.orElse("a PNG or JSON resource");
				violations.add(new AssetViolation(code(), reference.sourcePath(),
						"Property '" + reference.property() + "' references forever:" + reference.resourcePath()
								+ ", but the target " + expected + " does not exist."));
			}
		}
		return violations;
	}

	private static String relativeAssetPath(Path root, Path target) {
		return root.toAbsolutePath().normalize().relativize(target.toAbsolutePath().normalize())
				.toString().replace(target.getFileSystem().getSeparator(), "/");
	}
}
