/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.foundation.data.recipe;

import com.google.gson.JsonObject;

import io.github.shulej.createsifter.CreateSifter;
import io.github.shulej.createsifter.ModRecipeTypes;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipeSerializer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Runtime bridge between the raw recipe JSON stored in the server config
 * (SifterConfig.CUSTOM_RECIPES) and live SiftingRecipe objects.
 *
 * Recipes edited via the config screen are kept as JSON in the config file and
 * deserialised here (cached, invalidated on every config write). The cache is
 * safe on the client too: single-player runs in one JVM, so the same config
 * values are visible to both threads.
 *
 * 配方文件级修改的运行时桥梁：配置中以 JSON 保存，此处反序列化为 SiftingRecipe 并缓存。
 */
public final class CustomRecipeStore {

	/** Ids of player-created recipes: createsifter:custom/<n>. */
	public static final String CUSTOM_PREFIX = "createsifter:custom/";

	private CustomRecipeStore() {}

	private static volatile int instanceGen = -1;
	private static volatile Object instanceSource;
	private static volatile Map<String, SiftingRecipe> instanceCache = Map.of();

	/** @return recipe id -> deserialised recipe for every customised / new recipe (never null). */
	public static Map<String, SiftingRecipe> instances() {
		// Rebuild when the config generation changes (UI writes) OR when the
		// parsed table identity changes (external file reloads that do not bump
		// the generation counter).
		Object source = SifterConfig.customRecipesSource();
		int gen = SifterConfig.recipeTableGeneration();
		if (gen != instanceGen || source != instanceSource) {
			Map<String, SiftingRecipe> map = new HashMap<>();
			for (Map.Entry<String, JsonObject> e : SifterConfig.customRecipes().entrySet()) {
				try {
					map.put(e.getKey(), deserialize(e.getKey(), e.getValue()));
				} catch (Throwable t) {
					CreateSifter.LOGGER.warn("Skipping invalid custom sifting recipe {}: {}", e.getKey(),
							t.toString());
				}
			}
			instanceCache = map;
			instanceSource = source;
			instanceGen = gen;
		}
		return instanceCache;
	}

	private static SiftingRecipe deserialize(String id, JsonObject json) {
		ResourceLocation rid = ResourceLocation.tryParse(id);
		if (rid == null)
			throw new IllegalArgumentException("bad recipe id: " + id);
		SiftingRecipeSerializer ser = ModRecipeTypes.SIFTING.getSerializer();
		return ser.fromJson(rid, json);
	}

	/** Lowest unused {@code createsifter:custom/<n>} id for a brand new recipe. */
	public static String nextCustomId() {
		int max = 0;
		for (String id : SifterConfig.customRecipes().keySet()) {
			if (id.startsWith(CUSTOM_PREFIX)) {
				try {
					max = Math.max(max, Integer.parseInt(id.substring(CUSTOM_PREFIX.length())));
				} catch (NumberFormatException ignored) {
				}
			}
		}
		return CUSTOM_PREFIX + (max + 1);
	}

	/** Serialise a recipe to the same json the datapack loader would produce. */
	public static JsonObject toJson(SiftingRecipe recipe) {
		SiftingRecipeSerializer ser = ModRecipeTypes.SIFTING.getSerializer();
		JsonObject json = new JsonObject();
		ser.write(json, recipe);
		return json;
	}
}
