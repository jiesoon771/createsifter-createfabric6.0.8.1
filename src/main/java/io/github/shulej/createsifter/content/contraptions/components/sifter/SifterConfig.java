package io.github.shulej.createsifter.content.contraptions.components.sifter;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SifterConfig {

	// Server-side controls (authoritative on dedicated servers)
	public static ForgeConfigSpec.EnumValue<Difficulty> DIFFICULTY;
	public static ForgeConfigSpec.DoubleValue CUSTOM_CHANCE_MULTIPLIER;
	public static ForgeConfigSpec.DoubleValue CUSTOM_TIME_MULTIPLIER;
	/** Per-recipe output overrides. Key = recipe id "|" output index. Value = "chance;count". */
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> RECIPE_OVERRIDES;
	/** Master switch for the optional netherite sifting chain (soul_soil -> ancient debris / scrap / ingot). Off by default. */
	public static ForgeConfigSpec.BooleanValue ENABLE_NETHERITE_SIFT;

	// Common controls (mechanical block behaviour)
	public static ForgeConfigSpec.DoubleValue SIFTER_STRESS_IMPACT;
	public static ForgeConfigSpec.DoubleValue SIFTER_MINIMUM_SPEED;
	public static ForgeConfigSpec.IntValue SIFTER_OUTPUT_CAPACITY;

	// Client controls (rendering)
	public static ForgeConfigSpec.BooleanValue SIFTER_RENDER_SIFTED_BLOCK;
	public static ForgeConfigSpec.BooleanValue SIFTER_RENDER_MOVING_MESH;

	public static void registerServerConfig(ForgeConfigSpec.Builder SERVER_BUILDER) {
		DIFFICULTY = SERVER_BUILDER
				.comment("Difficulty preset for sifting recipes:",
						"  ultra  - output chance x1.5, processing time x0.5 (easiest)",
						"  high   - original behaviour (default)",
						"  medium - output chance x0.6, processing time x1.5",
						"  low    - output chance x0.35, processing time x2.0 (hardest)",
						"  custom - use customChanceMultiplier / customTimeMultiplier below")
				.defineEnum("difficulty", Difficulty.HIGH);
		CUSTOM_CHANCE_MULTIPLIER = SERVER_BUILDER
				.comment("Custom output chance multiplier (0.05 - 1.0; used only when difficulty = custom)")
				.defineInRange("customChanceMultiplier", 0.6, 0.05, 1.0);
		CUSTOM_TIME_MULTIPLIER = SERVER_BUILDER
				.comment("Custom processing time multiplier (0.1 - 10.0; used only when difficulty = custom)")
				.defineInRange("customTimeMultiplier", 1.5, 0.1, 10.0);
		RECIPE_OVERRIDES = SERVER_BUILDER
				.comment("Per-recipe output overrides. Format per entry:",
						"  <recipeId>|<outputIndex>|<chance0to100>|<count>",
						"Example: createsifter:sifting/andesite_sift|0|75|3",
						"An empty list disables all overrides (use the config screen to edit).")
				.defineList("recipeOverrides", List.of(), obj -> obj instanceof String);
		ENABLE_NETHERITE_SIFT = SERVER_BUILDER
				.comment("Master switch for the optional netherite sifting chain:",
						"  sifting soul_soil with the advanced brass mesh produces",
						"  ancient_debris, netherite_scrap and (very rarely) netherite_ingot.",
						"  Off by default; turn it on to enable the netherite drop chain.")
				.define("enableNetheriteSift", false);
	}

	public static void registerCommonConfig(ForgeConfigSpec.Builder COMMON_BUILDER) {
		COMMON_BUILDER.comment("Settings for the mechanical sifter").push("sifter");
		SIFTER_STRESS_IMPACT = COMMON_BUILDER
				.comment("Stress impact")
				.defineInRange("stressImpact", 4.0, 0.0, 64.0);
		SIFTER_MINIMUM_SPEED = COMMON_BUILDER
				.comment("Minimum required speed")
				.defineInRange("minimumSpeed", 1, 0.0, 254);
		SIFTER_OUTPUT_CAPACITY = COMMON_BUILDER
				.comment("Output item capacity (applies to newly placed sifters)")
				.defineInRange("outputCapacity", 16, 1, Integer.MAX_VALUE);
		COMMON_BUILDER.pop();
	}

	public static void registerClientConfig(ForgeConfigSpec.Builder CLIENT_BUILDER){
		CLIENT_BUILDER.comment("Settings for the mechanical sifter").push("sifter");

		SIFTER_RENDER_SIFTED_BLOCK = CLIENT_BUILDER
				.comment("Render sifted block").define("renderSiftedBlock",true);

		SIFTER_RENDER_MOVING_MESH = CLIENT_BUILDER
				.comment("Render moving mesh").define("renderMovingMesh",true);

		CLIENT_BUILDER.pop();
	}

	public static float customChanceMultiplier() {
		if (CUSTOM_CHANCE_MULTIPLIER == null) return 0.6f;
		return CUSTOM_CHANCE_MULTIPLIER.get().floatValue();
	}

	public static float customTimeMultiplier() {
		if (CUSTOM_TIME_MULTIPLIER == null) return 1.5f;
		return CUSTOM_TIME_MULTIPLIER.get().floatValue();
	}

	/** Max per-success amount of an override; never above a normal item stack size. */
	public static final int MAX_OVERRIDE_COUNT = 64;

	// Parsed override cache: rebuilt lazily when the config list reference changes
	// (UI writes swap the whole list; external file edits reload it too).
	private static volatile List<? extends String> overrideListSource;
	private static volatile Map<String, int[]> overrideListParsed;
	/** Bumped on every UI write so caches keyed on the override table can invalidate. */
	private static volatile int overrideGeneration;

	/** Monotonic version of the override table; changes whenever setOverride writes. */
	public static int overrideGeneration() {
		return overrideGeneration;
	}

	/**
	 * Resolve an override for recipeId's output at outputIndex.
	 * @return [chancePercent 0-100, count >= 1] or null if no override exists.
	 */
	public static int[] getOverride(String recipeId, int outputIndex) {
		if (RECIPE_OVERRIDES == null) return null;
		return overrides().get(recipeId + "|" + outputIndex);
	}

	private static Map<String, int[]> overrides() {
		List<? extends String> current = RECIPE_OVERRIDES.get();
		if (current != overrideListSource) {
			Map<String, int[]> parsed = new HashMap<>();
			for (Object o : current) {
				if (!(o instanceof String)) continue;
				String[] parts = ((String) o).split("\\|");
				if (parts.length != 4) continue;
				int idx = parseInt(parts[1], -1);
				if (idx < 0) continue; // drop malformed entries
				parsed.put(parts[0] + "|" + idx,
						new int[]{clampInt(parseInt(parts[2], 100), 0, 100),
								clampInt(parseInt(parts[3], 1), 1, MAX_OVERRIDE_COUNT)});
			}
			overrideListParsed = parsed;
			overrideListSource = current;
		}
		return overrideListParsed;
	}

	/** Set (or remove, when null) an override and persist the change. */
	public static void setOverride(String recipeId, int outputIndex, Integer chancePercent, Integer count) {
		if (RECIPE_OVERRIDES == null) return;
		Map<String, int[]> map = new HashMap<>();
		List<? extends String> cur = RECIPE_OVERRIDES.get();
		if (cur != null) {
			for (Object o : cur) {
				if (!(o instanceof String)) continue;
				String[] parts = ((String) o).split("\\|");
				if (parts.length != 4) continue;
				int idx = parseInt(parts[1], -1);
				if (idx < 0) continue; // drop malformed entries
				map.put(parts[0] + "|" + idx, new int[]{parseInt(parts[2], 100), parseInt(parts[3], 1)});
			}
		}
		String key = recipeId + "|" + outputIndex;
		if (chancePercent == null && count == null) {
			map.remove(key);
		} else {
			int[] prev = map.getOrDefault(key, new int[]{100, 1});
			int chance = chancePercent != null ? clampInt(chancePercent, 0, 100) : prev[0];
			int cnt = count != null ? clampInt(count, 1, MAX_OVERRIDE_COUNT) : prev[1];
			map.put(key, new int[]{chance, cnt});
		}
		RECIPE_OVERRIDES.set(map.entrySet().stream()
				.map(e -> e.getKey() + "|" + e.getValue()[0] + "|" + e.getValue()[1])
				.toList());
		overrideListSource = null; // force reparse on next read
		overrideGeneration++;
	}

	private static int parseInt(String s, int fallback) {
		try {
			return Integer.parseInt(s.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static int clampInt(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}
