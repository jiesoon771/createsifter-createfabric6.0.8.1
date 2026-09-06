/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.sifter;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SifterConfig {

	// Server-side controls (authoritative on dedicated servers)
	public static ForgeConfigSpec.EnumValue<Difficulty> DIFFICULTY;
	public static ForgeConfigSpec.DoubleValue CUSTOM_CHANCE_MULTIPLIER;
	public static ForgeConfigSpec.DoubleValue CUSTOM_TIME_MULTIPLIER;
	/** Per-recipe output overrides. Key = recipe id "|" output index. Value = "chance;count". */
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> RECIPE_OVERRIDES;
	/**
	 * Recipe-file edits made through the config screen.
	 * Format per entry: {@code <recipeId>||<json recipe>}.
	 * A recipeId matching a built-in recipe replaces it; any other id adds a new recipe.
	 */
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_RECIPES;
	/** Built-in recipes removed via the config screen. Format per entry: {@code <recipeId>}. */
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> DELETED_RECIPES;
	/** Master lock for the recipe-edit screen; see the config comment. */
	public static ForgeConfigSpec.BooleanValue RECIPE_EDIT_LOCKED;
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
		CUSTOM_RECIPES = SERVER_BUILDER
				.comment("Recipe-file edits made through the config screen.",
						"Format per entry:  <recipeId>||<json recipe>",
						"A recipeId matching a built-in recipe replaces that recipe;",
						"any other recipeId adds a brand new recipe (createsifter:custom/<n>).",
						"Use the config screen's Reset All Recipes button to restore the defaults.")
				.defineList("customRecipes", List.of(), obj -> obj instanceof String);
		DELETED_RECIPES = SERVER_BUILDER
				.comment("Built-in recipes removed through the config screen.",
						"Format per entry:  <recipeId>",
						"Use the config screen's Reset All Recipes button to restore them.")
				.defineList("deletedRecipes", List.of(), obj -> obj instanceof String);
		RECIPE_EDIT_LOCKED = SERVER_BUILDER
				.comment("Master lock for the recipe-edit screen. While true nobody can",
						"change difficulty, overrides or recipes without cheat/op permission,",
						"and turning the lock off itself also requires cheat/op permission.",
						"Turning it on requires no permission, so any player can lock the",
						"world's recipes to constrain themselves.")
				.define("recipeEditLocked", false);
		ENABLE_NETHERITE_SIFT = SERVER_BUILDER
				.comment("Master switch for the netherite sifting chain:",
						"  sifting crushed_basalt with the sturdy / advanced sturdy",
						"  mesh produces ancient_debris and netherite_scrap.",
						"  On by default; turn it off to disable the netherite drop chain.")
				.define("enableNetheriteSift", true);
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

	// --- Recipe-file edits (customRecipes / deletedRecipes) ---
	// Same cache pattern as the override table: parsed lazily whenever the config
	// list reference changes (UI writes swap the whole list; external edits reload).
	private static volatile List<? extends String> customListSource;
	private static volatile Map<String, JsonObject> customListParsed;
	private static volatile List<? extends String> deletedListSource;
	private static volatile Set<String> deletedListParsed;
	/** Bumped on every custom/deleted write so dependent caches can invalidate. */
	private static volatile int recipeTableGeneration;

	public static int recipeTableGeneration() {
		return recipeTableGeneration;
	}

	/** @return recipe id -> raw json of every customised / new recipe (never null). */
	public static Map<String, JsonObject> customRecipes() {
		if (CUSTOM_RECIPES == null) return Map.of();
		List<? extends String> current = CUSTOM_RECIPES.get();
		if (current != customListSource) {
			Map<String, JsonObject> parsed = new HashMap<>();
			for (Object o : current) {
				if (!(o instanceof String)) continue;
				String s = ((String) o).trim();
				int sep = s.indexOf("||");
				if (sep <= 0 || sep >= s.length() - 2) continue;
				String id = s.substring(0, sep);
				try {
					parsed.put(id, GsonHelper.parse(s.substring(sep + 2)).getAsJsonObject());
				} catch (RuntimeException ignored) {
					// malformed entry: skip it rather than break the whole table
				}
			}
			customListParsed = parsed;
			customListSource = current;
		}
		return customListParsed;
	}

	/**
	 * Identity token of the currently parsed custom table: a fresh instance
	 * after every UI write AND every external file reload. Used as a cache key
	 * by {@link io.github.shulej.createsifter.foundation.data.recipe.CustomRecipeStore}
	 * and {@link io.github.shulej.createsifter.ModRecipeTypes}.
	 */
	public static Object customRecipesSource() {
		return customRecipes();
	}

	/** Parsed deleted-recipe id set, rebuilt whenever the config list reference changes. */
	private static Set<String> deletedParsed() {
		if (DELETED_RECIPES == null) return Set.of();
		List<? extends String> current = DELETED_RECIPES.get();
		if (current != deletedListSource) {
			Set<String> parsed = new HashSet<>();
			for (Object o : current)
				if (o instanceof String) parsed.add(((String) o).trim());
			deletedListParsed = parsed;
			deletedListSource = current;
		}
		return deletedListParsed;
	}

	/**
	 * Identity token of the currently parsed deleted table (fresh instance after
	 * every UI write AND every external file reload). Used as a cache key.
	 */
	public static Object deletedRecipesSource() {
		return deletedParsed();
	}

	public static boolean isDeleted(String recipeId) {
		if (DELETED_RECIPES == null) return false;
		return deletedParsed().contains(recipeId);
	}

	public static void putCustomRecipe(String recipeId, JsonObject json) {
		if (CUSTOM_RECIPES == null) return;
		Map<String, JsonObject> map = new HashMap<>(customRecipes());
		map.put(recipeId, json);
		writeCustomList(map);
	}

	public static void removeCustomRecipe(String recipeId) {
		if (CUSTOM_RECIPES == null) return;
		Map<String, JsonObject> map = new HashMap<>(customRecipes());
		if (map.remove(recipeId) != null)
			writeCustomList(map);
	}

	private static void writeCustomList(Map<String, JsonObject> map) {
		CUSTOM_RECIPES.set(map.entrySet().stream()
				.map(e -> e.getKey() + "||" + e.getValue().toString())
				.toList());
		customListSource = null;
		recipeTableGeneration++;
	}

	/** Mark a built-in recipe as removed (or restored when deleted == false). */
	public static void setDeleted(String recipeId, boolean deleted) {
		if (DELETED_RECIPES == null) return;
		Set<String> set = new HashSet<>();
		List<? extends String> cur = DELETED_RECIPES.get();
		if (cur != null)
			for (Object o : cur)
				if (o instanceof String) set.add(((String) o).trim());
		boolean changed = deleted ? set.add(recipeId) : set.remove(recipeId);
		if (changed) {
			DELETED_RECIPES.set(List.copyOf(set));
			deletedListSource = null;
			recipeTableGeneration++;
		}
	}

	/** Remove every override belonging to one recipe (used when its outputs change shape). */
	public static void clearOverrides(String recipeId) {
		if (RECIPE_OVERRIDES == null) return;
		Map<String, int[]> map = new HashMap<>(overrides());
		boolean removed = map.keySet().removeIf(k -> k.startsWith(recipeId + "|"));
		if (removed) {
			RECIPE_OVERRIDES.set(map.entrySet().stream()
					.map(e -> e.getKey() + "|" + e.getValue()[0] + "|" + e.getValue()[1])
					.toList());
			overrideListSource = null;
			overrideGeneration++;
		}
	}

	/**
	 * In-memory mirror of the edit lock. Set synchronously when the UI locks the
	 * recipes so the screen updates immediately instead of waiting for the
	 * asynchronous server-thread write; once true it never goes back, matching
	 * the permanent-lock design.
	 */
	private static volatile boolean recipeEditLockedMirror;

	/**
	 * Clear the in-memory lock mirror when a new world loads so the previous
	 * world's lock never leaks into the next one. Called on SERVER_STARTED,
	 * after the current world's server config has been loaded; the live value
	 * is then re-read from that world's own config file.
	 */
	public static void resetRecipeEditLockMirror() {
		recipeEditLockedMirror = false;
	}

	public static boolean isRecipeEditLocked() {
		if (recipeEditLockedMirror) return true;
		return RECIPE_EDIT_LOCKED != null && RECIPE_EDIT_LOCKED.get();
	}

	/**
	 * Lock the recipe-edit screen permanently. The UI only ever writes true;
	 * once locked there is no in-game way back (manual config-file editing plus
	 * a restart is the only escape).
	 */
	public static void setRecipeEditLocked(boolean locked) {
		if (!locked) return; // permanent: the UI never unlocks
		recipeEditLockedMirror = true;
		if (RECIPE_EDIT_LOCKED != null)
			RECIPE_EDIT_LOCKED.set(true);
	}

	/** Restore every recipe-related edit (overrides, customised recipes, deletions) to the defaults. */
	public static void resetAllRecipeEdits() {
		if (RECIPE_OVERRIDES != null) {
			RECIPE_OVERRIDES.set(List.of());
			overrideListSource = null;
			overrideGeneration++;
		}
		if (CUSTOM_RECIPES != null) {
			CUSTOM_RECIPES.set(List.of());
			customListSource = null;
			recipeTableGeneration++;
		}
		if (DELETED_RECIPES != null) {
			DELETED_RECIPES.set(List.of());
			deletedListSource = null;
			recipeTableGeneration++;
		}
	}

	// --- Session rollback (config screen Cancel) ---
	// The config screen snapshots every setting it can change when it opens;
	// "Cancel" restores them so a discarded session leaves no partial changes.
	private static volatile boolean recipeSnapshotTaken;
	private static volatile Difficulty recipeSnapshotDifficulty;
	private static volatile Double recipeSnapshotChanceMultiplier;
	private static volatile Double recipeSnapshotTimeMultiplier;
	private static volatile List<String> recipeSnapshotOverrides;
	private static volatile List<String> recipeSnapshotCustom;
	private static volatile List<String> recipeSnapshotDeleted;

	/**
	 * Snapshot every config-screen-editable value. Call once when a config
	 * screen session begins so "Cancel" can roll the whole session back.
	 * The recipe-edit lock is intentionally NOT snapshotted: it is permanent
	 * and must survive any session, so Cancel can never undo it.
	 */
	public static void snapshotRecipeEdits() {
		recipeSnapshotDifficulty = DIFFICULTY != null ? DIFFICULTY.get() : null;
		recipeSnapshotChanceMultiplier = CUSTOM_CHANCE_MULTIPLIER != null ? CUSTOM_CHANCE_MULTIPLIER.get() : null;
		recipeSnapshotTimeMultiplier = CUSTOM_TIME_MULTIPLIER != null ? CUSTOM_TIME_MULTIPLIER.get() : null;
		recipeSnapshotOverrides = RECIPE_OVERRIDES != null ? new ArrayList<>(RECIPE_OVERRIDES.get()) : null;
		recipeSnapshotCustom = CUSTOM_RECIPES != null ? new ArrayList<>(CUSTOM_RECIPES.get()) : null;
		recipeSnapshotDeleted = DELETED_RECIPES != null ? new ArrayList<>(DELETED_RECIPES.get()) : null;
		recipeSnapshotTaken = true;
	}

	/**
	 * Restore every config-screen-editable value to the snapshot taken when the
	 * config screen opened. No-op when no snapshot exists.
	 */
	public static void restoreRecipeEdits() {
		if (!recipeSnapshotTaken) return;
		if (DIFFICULTY != null && recipeSnapshotDifficulty != null)
			DIFFICULTY.set(recipeSnapshotDifficulty);
		if (CUSTOM_CHANCE_MULTIPLIER != null && recipeSnapshotChanceMultiplier != null)
			CUSTOM_CHANCE_MULTIPLIER.set(recipeSnapshotChanceMultiplier);
		if (CUSTOM_TIME_MULTIPLIER != null && recipeSnapshotTimeMultiplier != null)
			CUSTOM_TIME_MULTIPLIER.set(recipeSnapshotTimeMultiplier);
		if (RECIPE_OVERRIDES != null && recipeSnapshotOverrides != null) {
			RECIPE_OVERRIDES.set(recipeSnapshotOverrides);
			overrideListSource = null;
			overrideGeneration++;
		}
		if (CUSTOM_RECIPES != null && recipeSnapshotCustom != null) {
			CUSTOM_RECIPES.set(recipeSnapshotCustom);
			customListSource = null;
			recipeTableGeneration++;
		}
		if (DELETED_RECIPES != null && recipeSnapshotDeleted != null) {
			DELETED_RECIPES.set(recipeSnapshotDeleted);
			deletedListSource = null;
			recipeTableGeneration++;
		}
	}
}

