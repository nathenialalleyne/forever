package dev.forever.tools.assets;

import java.nio.file.Path;

/** A regular file discovered below the asset namespace. */
record AssetFile(Path path, Path relativePath, String stem) {

	static AssetFile of(Path path, Path relativePath) {
		String fileName = relativePath.getFileName().toString();
		int extensionIndex = fileName.lastIndexOf('.');
		String stem = extensionIndex > 0 ? fileName.substring(0, extensionIndex) : fileName;
		return new AssetFile(path, relativePath, stem);
	}
}
