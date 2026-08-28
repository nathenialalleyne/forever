package dev.forever.tools.assets;

import java.io.IOException;
import java.util.List;

/** Contract for one distinct, reportable validation check. */
interface AssetRule {
	ViolationCode code();

	List<AssetViolation> check(AssetValidationContext context) throws IOException, UsageException;
}
