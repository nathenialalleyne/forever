package dev.forever.tools.assets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Maps Minecraft resource locations to paths below the Forever asset namespace. */
final class ResourceReferenceResolver {

	ResolutionResult resolve(Path assetsDirectory, ResourceReference reference) {
		String resourcePath = reference.resourcePath();
		if (resourcePath.isBlank() || resourcePath.startsWith("/") || resourcePath.contains("\\")) {
			return ResolutionResult.invalid("resource path is empty or not a safe relative path");
		}
		for (String part : resourcePath.split("/", -1)) {
			if (part.isEmpty() || ".".equals(part) || "..".equals(part)) {
				return ResolutionResult.invalid("resource path contains an unsafe segment");
			}
		}
		List<Path> candidates = new ArrayList<>();
		if (reference.kind() == ReferenceKind.TEXTURE || reference.kind() == ReferenceKind.UNKNOWN) {
			candidates.add(target(assetsDirectory, "textures", resourcePath, ".png"));
		}
		if (reference.kind() == ReferenceKind.MODEL || reference.kind() == ReferenceKind.UNKNOWN) {
			candidates.add(target(assetsDirectory, "models", resourcePath, ".json"));
		}
		return ResolutionResult.valid(candidates);
	}

	private static Path target(Path root, String directory, String resourcePath, String suffix) {
		String path = resourcePath.endsWith(suffix) ? resourcePath : resourcePath + suffix;
		return root.resolve(directory).resolve(path).normalize();
	}

	record ResolutionResult(List<Path> candidates, String invalidReason) {
		static ResolutionResult valid(List<Path> candidates) {
			return new ResolutionResult(List.copyOf(candidates), "");
		}

		static ResolutionResult invalid(String reason) {
			return new ResolutionResult(List.of(), reason);
		}

		boolean valid() {
			return invalidReason.isBlank();
		}
	}
}
