package dev.forever.tools.assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Walks one asset namespace once and bounds all later checks to that input. */
final class AssetInventoryScanner {

	private AssetInventoryScanner() {
	}

	static AssetInventory scan(Path assetsDirectory) throws IOException {
		Path root = assetsDirectory.toAbsolutePath().normalize();
		List<Path> paths;
		try (Stream<Path> walk = Files.walk(root)) {
			paths = walk.sorted(Comparator.comparing(path -> relativeName(root, path))).toList();
		}
		List<AssetFile> files = new ArrayList<>();
		List<AssetFile> productionPngs = new ArrayList<>();
		List<AssetFile> referenceJsons = new ArrayList<>();
		List<Path> directories = new ArrayList<>();
		for (Path path : paths) {
			Path relativePath = root.relativize(path);
			if (Files.isDirectory(path)) {
				if (!relativePath.toString().isEmpty()) {
					directories.add(relativePath);
				}
				continue;
			}
			if (!Files.isRegularFile(path)) {
				continue;
			}
			if (isToolingMarker(relativePath)) {
				// Dot-files such as .gitkeep exist only so that git tracks an
				// otherwise empty production directory. They are tooling markers,
				// not assets, so naming and manifest rules must not apply to them.
				continue;
			}
			AssetFile file = AssetFile.of(path, relativePath);
			files.add(file);
			String name = relativeName(root, path);
			if (name.endsWith(".png")) {
				productionPngs.add(file);
			}
			if (name.endsWith(".json") && isReferenceJson(relativePath)) {
				referenceJsons.add(file);
			}
		}
		return new AssetInventory(files, productionPngs, referenceJsons, directories);
	}

	private static boolean isToolingMarker(Path relativePath) {
		return relativePath.getFileName().toString().startsWith(".");
	}

	private static boolean isReferenceJson(Path relativePath) {
		String name = relativePath.toString().replace(relativePath.getFileSystem().getSeparator(), "/");
		return name.startsWith("models/item/") || name.startsWith("models/block/")
				|| name.startsWith("items/") || name.startsWith("blockstates/");
	}

	private static String relativeName(Path root, Path path) {
		return root.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
	}
}
