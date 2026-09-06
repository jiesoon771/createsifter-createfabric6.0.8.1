/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter;

import net.createmod.catnip.lang.Lang;

import io.github.fabricators_of_create.porting_lib.util.ShapedRecipeUtil;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipeSerializer;
import io.github.shulej.createsifter.foundation.data.recipe.CustomRecipeStore;
import io.github.shulej.createsifter.foundation.data.recipe.SiftingRecipeBuilder;
import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public enum ModRecipeTypes implements IRecipeTypeInfo {
	SIFTING(SiftingRecipe::new);

	private final ResourceLocation id;
	private final RecipeSerializer<?> serializerObject;
	@Nullable
	private final RecipeType<?> typeObject;
	private final Supplier<RecipeType<?>> type;

	ModRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier, boolean registerType) {
		String name = Lang.asId(name());
		id = CreateSifter.asResource(name);
		serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		if (registerType) {
			typeObject = typeSupplier.get();
			Registry.register(BuiltInRegistries.RECIPE_TYPE, id, typeObject);
		} else {
			typeObject = null;
		}
		type = typeSupplier;
	}
	ModRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
		String name = Lang.asId(name());
		id = CreateSifter.asResource(name);
		serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		typeObject = simpleType(id);
		Registry.register(BuiltInRegistries.RECIPE_TYPE, id, typeObject);
		type = () -> typeObject;
	}

	ModRecipeTypes(SiftingRecipeBuilder.SiftingRecipeFactory processingFactory) {
		this(() -> new SiftingRecipeSerializer(processingFactory));
	}

	public static <T extends Recipe<?>> RecipeType<T> simpleType(ResourceLocation id) {
		String stringId = id.toString();
		return new RecipeType<T>() {
			@Override
			public String toString() {
				return stringId;
			}
		};
	}

	public static void register() {
		ShapedRecipeUtil.setCraftingSize(9, 9);
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeSerializer<?>> T getSerializer() {
		return (T) serializerObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeType<?>> T getType() {
		return (T) type.get();
	}

	public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level world) {
		return world.getRecipeManager()
				.getRecipeFor(getType(), inv, world);
	}

	public Optional<SiftingRecipe> find(Container inv, Level world, boolean waterlogged, float speed) {
		return find(inv, world, waterlogged, speed, false);
	}

	/**
	 * @param advanced whether the sifter holds an advanced mesh; recipes whose mesh
	 *                 ingredient is a plain mesh must not match for advanced sifters
	 *                 (and vice versa), keeping search and cached-match checks consistent.
	 */
	public Optional<SiftingRecipe> find(Container inv, Level world, boolean waterlogged, float speed, boolean advanced) {
		// Recipes are synced to clients, so this is safe on both sides: the client
		// uses it to predict hand sifting (BaseMesh.use). Machine recipe selection
		// stays server-driven (SifterBlockEntity.tick guards client ticks itself).
		// Customised / new recipes from the config table are merged in here.
		return customizedSiftingRecipes(world).stream()
				.filter(siftingRecipe -> siftingRecipe.matches(inv, world, waterlogged, speed, advanced))
				.findAny();
	}

	/**
	 * The sifting recipe set as seen by gameplay and by the config screen:
	 * built-in datapack recipes, minus the ones removed via the config screen,
	 * with player edits replacing the originals and brand-new recipes appended.
	 * The merged list is cached per config generation: machine matching calls
	 * this every tick, so rebuilding+sorting each time is wasted work.
	 *
	 * 筛分配方全集：内置配方（去除已删除的），叠加玩家编辑覆盖，并追加新增配方。
	 */
	private static volatile int mergedRecipesGen = -1;
	private static volatile Object mergedCustomSource;
	private static volatile Object mergedDeletedSource;
	private static volatile List<SiftingRecipe> mergedRecipes = List.of();
	/** The built-in list (from the recipe manager) the current merged cache was built from. */
	private static volatile List<SiftingRecipe> mergedBuiltinRef = null;

	public static List<SiftingRecipe> customizedSiftingRecipes(Level world) {
		int gen = SifterConfig.recipeTableGeneration();
		// The generation counter only moves on UI writes; external file reloads
		// (server admins editing the TOML) re-parse without bumping it, so the
		// parsed-table identities are part of the cache key too.
		Object customSource = SifterConfig.customRecipesSource();
		Object deletedSource = SifterConfig.deletedRecipesSource();
		if (gen == mergedRecipesGen && customSource == mergedCustomSource && deletedSource == mergedDeletedSource) {
			// Server-side RecipeManager changes always happen in tandem with
			// invalidateRecipeCache() (pushCustomRecipesToServer), so the key above
			// is complete there. A remote client's manager, however, is replaced by
			// a /reload sync packet without the generation moving — verify the
			// recipe set is still the one we merged from before trusting the cache.
			if (!world.isClientSide() || clientManagerMatches(world))
				return mergedRecipes;
		}
		List<SiftingRecipe> builtin = new ArrayList<>(
				world.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SIFTING.getType()));
		List<SiftingRecipe> out = mergeCustom(builtin);
		mergedRecipes = out;
		mergedRecipesGen = gen;
		mergedCustomSource = customSource;
		mergedDeletedSource = deletedSource;
		mergedBuiltinRef = builtin;
		return out;
	}

	/**
	 * True if the (client) recipe manager still holds the exact built-in set the
	 * current merged cache was built from. RecipeManager.replaceRecipes builds
	 * fresh recipe instances for a new table, so object identity of an element
	 * (checked at both ends, with sizes already equal) is enough to prove the
	 * set instance — and therefore the manager table — is unchanged.
	 *
	 * 客户端 manager 是否仍然持有构建当前合并缓存时的同一份内置配方集。
	 */
	private static boolean clientManagerMatches(Level world) {
		List<SiftingRecipe> cached = mergedBuiltinRef;
		if (cached == null)
			return false;
		List<SiftingRecipe> now = new ArrayList<>(
				world.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SIFTING.getType()));
		if (now.size() != cached.size())
			return false;
		if (now.isEmpty())
			return true;
		return now.get(0) == cached.get(0) && now.get(now.size() - 1) == cached.get(cached.size() - 1);
	}

	/**
	 * Shared merge rule: built-in recipes minus the deleted ones, with player
	 * edits replacing the originals and brand-new recipes appended. Used both by
	 * the matching cache above and by the server-side RecipeManager injection, so
	 * gameplay matching and what clients receive stay identical.
	 *
	 * 合并规则（匹配缓存与服务端注入共用，保证两侧配方集一致）。
	 */
	public static List<SiftingRecipe> mergeCustom(List<SiftingRecipe> builtin) {
		Map<String, SiftingRecipe> custom = CustomRecipeStore.instances();
		boolean anyDeleted = false;
		for (SiftingRecipe r : builtin)
			if (SifterConfig.isDeleted(r.getId().toString())) {
				anyDeleted = true;
				break;
			}
		if (custom.isEmpty() && !anyDeleted)
			return builtin;
		Set<String> builtinIds = new HashSet<>();
		for (SiftingRecipe r : builtin)
			builtinIds.add(r.getId().toString());
		List<SiftingRecipe> out = new ArrayList<>(builtin.size() + custom.size());
		for (SiftingRecipe r : builtin) {
			if (SifterConfig.isDeleted(r.getId().toString()))
				continue;
			SiftingRecipe replaced = custom.get(r.getId().toString());
			out.add(replaced != null ? replaced : r);
		}
		for (Map.Entry<String, SiftingRecipe> e : custom.entrySet())
			if (!builtinIds.contains(e.getKey()))
				out.add(e.getValue());
		out.sort(Comparator.comparing(a -> a.getId().toString()));
		return out;
	}

	/**
	 * Drop the merged-list cache. Called when the server RecipeManager is
	 * rewritten by {@link #pushCustomRecipesToServer}: the recipe content then
	 * changes without the config generation bumping.
	 */
	public static void invalidateRecipeCache() {
		mergedRecipesGen = -1;
	}

	/**
	 * Built-in sifting recipes exactly as loaded from datapacks — captured right
	 * after the RecipeManager is rebuilt (server start / /reload), before any of
	 * our injection runs. {@link #pushCustomRecipesToServer} merges against this
	 * snapshot instead of re-reading the live manager: once the manager holds the
	 * merged table, reading it back would treat customised / custom-only recipes
	 * as "built-in", so deletions, Reset All and logins could never take effect.
	 *
	 * 内置筛分配方基线：仅在 RecipeManager 由数据包重建后、本次注入前采集。
	 */
	private static volatile List<SiftingRecipe> pristineSiftingRecipes = null;

	/**
	 * True while a datapack reload is in progress (START_DATA_PACK_RELOAD fired,
	 * END_DATA_PACK_RELOAD not yet). Vanilla's reload chain re-syncs recipes to
	 * every player after the manager is rebuilt; without this flag that sync
	 * would merge against the OLD baseline and silently roll back datapack edits.
	 * The END handler clears it after capturing the fresh baseline.
	 *
	 * 数据包重载进行中标记：重载期间的逐玩家同步跳过注入，保住重建后的纯净 manager。
	 */
	private static volatile boolean baselineDirty = false;

	public static void setBaselineDirty(boolean dirty) {
		baselineDirty = dirty;
	}

	/**
	 * Record the built-in sifting recipes from a freshly rebuilt (datapack-only)
	 * RecipeManager. The empty-total-table guard skips captures that ran before
	 * the datapacks were applied — a wrongly-empty baseline would erase every
	 * built-in recipe on the next merge.
	 *
	 * 采集 pristine 基线；manager 尚未加载数据包时不采集。
	 */
	public static void capturePristineBaseline(MinecraftServer server) {
		if (server == null)
			return;
		RecipeManager rm = server.getRecipeManager();
		if (rm.getRecipes().isEmpty())
			return;
		pristineSiftingRecipes = List.copyOf(rm.getAllRecipesFor(SIFTING.getType()));
	}

	/**
	 * Merge the customised / deleted / new recipes into the server's own
	 * RecipeManager. Vanilla then syncs the resulting table to every client on
	 * login (and on datapack reload), so hand-sifting prediction and JEI see the
	 * same recipe set the server actually uses. Only SIFTING entries are touched;
	 * every other recipe type is left exactly as it was. Safe to call repeatedly.
	 *
	 * 把合并后的筛分配方注入服务端 RecipeManager，借原版同步机制下发到所有客户端。
	 */
	public static void pushCustomRecipesToServer(MinecraftServer server) {
		// A datapack reload is mid-flight: the manager was just (or is about to be)
		// rebuilt from datapacks, and merging with the stale baseline here would
		// roll back datapack edits. The END_DATA_PACK_RELOAD handler re-captures
		// the fresh baseline, clears the flag and pushes.
		if (server == null || baselineDirty)
			return;
		RecipeManager rm = server.getRecipeManager();
		List<Recipe<?>> all = new ArrayList<>(rm.getRecipes());
		// Merge against the pristine datapack snapshot, never against the current
		// manager contents: after the first injection the manager already holds the
		// merged table, so reading it back as "built-in" would pin removed recipes
		// back in and prevent Reset All from restoring the true originals.
		List<SiftingRecipe> baseline = pristineSiftingRecipes;
		if (baseline == null) {
			// No snapshot captured yet (push before any capture event): at that
			// point nothing has been injected, so the live manager is pristine.
			baseline = new ArrayList<>();
			for (Recipe<?> r : all)
				if (r.getType() == SIFTING.getType())
					baseline.add((SiftingRecipe) r);
		}
		all.removeIf(r -> r.getType() == SIFTING.getType());
		all.addAll(mergeCustom(baseline));
		rm.replaceRecipes(all);
		invalidateRecipeCache();
	}

	public static boolean shouldIgnoreAutomation(Recipe<?> recipe) {
		RecipeSerializer<?> serializer = recipe.getSerializer();
		return recipe.getId().getPath().endsWith("_manual_only");
	}
}

