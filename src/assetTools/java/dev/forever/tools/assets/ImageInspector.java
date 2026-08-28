package dev.forever.tools.assets;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/** Decodes PNGs through ImageIO and records format failures instead of hiding them. */
final class ImageInspector {

	ImageSummary inspect(Path path) {
		try {
			// ImageIO.read(File) owns its input stream. Keeping that ownership local avoids
			// treating the reader's close as a second close failure on newer JDKs.
			BufferedImage image = ImageIO.read(path.toFile());
			if (image == null) {
				return undecodable("ImageIO found no decoder for this file.");
			}
			ColourModelKind colourModel = classify(image.getColorModel());
			return new ImageSummary(image.getWidth(), image.getHeight(), colourModel,
					isFullyTransparent(image), "");
		} catch (IOException exception) {
			return undecodable(exception.getMessage() == null ? exception.getClass().getSimpleName()
					: exception.getMessage());
		}
	}

	private static ImageSummary undecodable(String detail) {
		return new ImageSummary(-1, -1, ColourModelKind.UNDECODABLE, false, detail);
	}

	private static ColourModelKind classify(ColorModel colorModel) {
		if (colorModel instanceof IndexColorModel) {
			return ColourModelKind.INDEXED;
		}
		int colourSpaceType = colorModel.getColorSpace().getType();
		if (colourSpaceType == ColorSpace.TYPE_CMYK) {
			return ColourModelKind.CMYK;
		}
		if (colourSpaceType == ColorSpace.TYPE_RGB && colorModel.getNumColorComponents() == 3) {
			return colorModel.hasAlpha() ? ColourModelKind.RGBA : ColourModelKind.RGB;
		}
		if (colourSpaceType == ColorSpace.TYPE_GRAY && colorModel.getNumColorComponents() == 1) {
			return colorModel.hasAlpha() ? ColourModelKind.GREYSCALE_ALPHA : ColourModelKind.GREYSCALE;
		}
		return ColourModelKind.OTHER;
	}

	private static boolean isFullyTransparent(BufferedImage image) {
		if (!image.getColorModel().hasAlpha()) {
			return false;
		}
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					return false;
				}
			}
		}
		return true;
	}
}
