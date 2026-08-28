package dev.forever.compat.matcha;

import java.util.Map;
import java.util.Set;

/** Package-private facts for the audited Matcha Flavoured 1.12 profile. */
final class MatchaProfile {
	static final String VERSION = "1.12";
	static final String PROFILE_ID = "matcha-flavoured:1.12";
	static final String ARCHIVE_SHA256 = "6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248";
	static final double MIN_PACK_FORMAT = 88.0;
	static final double MAX_PACK_FORMAT = 107.1;
	static final String DESCRIPTION = "klei's matcha flavoured 1.12 for 26.2";

	static final Map<String, String> FINGERPRINTS = Map.of(
			"pack.mcmeta", "0530019201fd54cc25a4c547685a45192a3c46892de7fcde62e72d548725d00d",
			"data/main/function/setup/scoreboard.mcfunction", "b67557db923d9083106be79523e997e875e152a646786a9d76211bd854f0730a",
			"data/endless_repairs/function/reset_breakable_repair_cost.mcfunction", "1a2e537e345a8c302584178c75ae762f4dbf5b5e75b63a05806091d00cee9967",
			"data/crafting/recipe/crystal_heart.json", "310d83dc935caf2dd41784448e1832e3cd3e91f66a221c4fd25e6c870bd5ece5",
			"data/main/advancement/tutorial/root.json", "eda08fd80df5967ecaaffe1c9c684206862836570bb75898be02417ed2a6a08e",
			"assets/minecraft/items/heart_container.json", "05347be8ff268e8f4f34aec63ff5a88a856661ecd9f0f3bd23f96c06f97afd46");

	static final String VERSION_OBJECTIVE = "version_number";
	static final String VERSION_SCORE_HOLDER = "current_version";
	static final int VERSION_SCORE = 112;
	static final Set<String> IDENTITY_COMPONENTS = Set.of(
			"minecraft:item_model",
			"minecraft:custom_model_data",
			"minecraft:entity_data");
	static final Set<String> ADVANCEMENT_MARKERS = Set.of(
			"main:tutorial/root",
			"main:mechanics/root");

	private MatchaProfile() {
	}
}
