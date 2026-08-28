package dev.forever.tools.assets;

import java.util.ArrayList;
import java.util.List;

/** Rejects undecodable, indexed, CMYK, and otherwise unsupported PNG colour models. */
final class ImageFormatCheck implements AssetRule {

	@Override
	public ViolationCode code() {
		return ViolationCode.UNSUPPORTED_IMAGE_FORMAT;
	}

	@Override
	public List<AssetViolation> check(AssetValidationContext context) {
		List<AssetViolation> violations = new ArrayList<>();
		for (ProductionAsset asset : context.productionAssets()) {
			ImageSummary image = asset.image();
			if (!image.supported()) {
				String detail = image.detail().isBlank() ? "" : " Detail: " + image.detail();
				violations.add(new AssetViolation(code(), pathName(asset),
						"Image must decode as RGB, RGBA, or greyscale-with-alpha; found "
								+ image.colourModel() + "." + detail));
			}
		}
		return violations;
	}

	private static String pathName(ProductionAsset asset) {
		return asset.file().relativePath().toString()
				.replace(asset.file().relativePath().getFileSystem().getSeparator(), "/");
	}
}
