package dev.forever.tools.assets;

/** The three lifecycle states permitted by the asset manifest contract. */
enum ManifestStatus {
	PLANNED("planned"),
	PLACEHOLDER("placeholder"),
	FINAL("final");

	private final String value;

	ManifestStatus(String value) {
		this.value = value;
	}

	static ManifestStatus parse(String value, int rowNumber) throws UsageException {
		for (ManifestStatus status : values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new UsageException("Manifest row " + rowNumber + " has unsupported status '" + value
				+ "'. Use planned, placeholder, or final.");
	}

	@Override
	public String toString() {
		return value;
	}
}
