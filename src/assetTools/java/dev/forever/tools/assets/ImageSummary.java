package dev.forever.tools.assets;

/** Image dimensions, decoded colour model, and transparency observation. */
record ImageSummary(int width, int height, ColourModelKind colourModel,
		boolean fullyTransparent, String detail) {

	ImageSummary {
		detail = detail == null ? "" : detail.replaceAll("\\R", " ");
	}

	boolean supported() {
		return colourModel == ColourModelKind.RGB || colourModel == ColourModelKind.RGBA
				|| colourModel == ColourModelKind.GREYSCALE_ALPHA;
	}

	String dimensionsLabel() {
		return width < 0 || height < 0 ? "unknown" : width + "x" + height;
	}
}
