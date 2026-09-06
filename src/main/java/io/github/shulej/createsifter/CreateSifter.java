/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter;

import com.simibubi.create.Create;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.CreateRegistrate;

import io.github.shulej.createsifter.content.contraptions.components.brass_sifter.BrassSifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.register.ModBlockEntities;
import io.github.shulej.createsifter.register.ModBlocks;
import io.github.shulej.createsifter.register.ModConfigs;
import io.github.shulej.createsifter.register.ModCreativeTabs;
import io.github.shulej.createsifter.register.ModItems;
import io.github.shulej.createsifter.register.ModTags;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateSifter implements ModInitializer {
	public static final String MODID = "createsifter";
	public static final String NAME = "Create Sifter";
	public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

	public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(CreateSifter.MODID);

	@Override
	public void onInitialize() {
		ModConfigs.register();
		ensurePerWorldServerConfigs();

		// A world switch must drop the previous world's in-memory lock mirror so
		// the lock never leaks into other worlds (each world has its own config).
		// Also record the pristine datapack baseline: the RecipeManager has just
		// been rebuilt from datapacks and nothing has been injected yet, and every
		// later merge/push must start from this snapshot, never from the live
		// (already-merged) manager.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SifterConfig.resetRecipeEditLockMirror();
			ModRecipeTypes.setBaselineDirty(false);
			ModRecipeTypes.capturePristineBaseline(server);
		});

		// Keep the server's RecipeManager in sync with the config-screen recipe
		// table: injected after every datapack load and right before each player's
		// login sync, so every client receives the customised recipe set (hand
		// sifting, JEI and deleted-recipe prediction all work on the synced table).
		// A data-pack reload rebuilds the manager from scratch — re-capture the
		// baseline there so edits to the datapacks themselves become the new
		// built-in originals, then re-inject.
		//
		// Event order inside a /reload (verified against fabric-lifecycle-events
		// 2.2.20 and the vanilla 1.20.1 bytecode):
		// 1. START_DATA_PACK_RELOAD fires at the head of
		//    MinecraftServer.reloadResources, before the manager is rebuilt.
		// 2. Inside the reload chain, PlayerList.reloadResources() re-syncs
		//    recipes to every player, firing SYNC_DATA_PACK_CONTENTS per player.
		//    The baselineDirty guard makes our own push no-op there, keeping the
		//    freshly rebuilt (datapack-only) manager pristine — otherwise that
		//    sync would re-merge the OLD baseline on top and silently roll back
		//    any datapack edits the admin made.
		// 3. END_DATA_PACK_RELOAD fires after the whole future completes: capture
		//    the now-pristine baseline, re-enable pushing, merge, and re-run the
		//    per-player sync so clients receive the merged table (step 2 already
		//    sent the unmerged one). PlayerList.reloadResources() only re-sends
		//    tags/recipes/advancements — it does not re-enter the datapack
		//    reload, so this cannot recurse.
		ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) ->
				ModRecipeTypes.setBaselineDirty(true));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (!success) {
				// A failed reload never replaced the manager; the previous baseline
				// is still valid. Only the in-flight guard must be cleared.
				ModRecipeTypes.setBaselineDirty(false);
				return;
			}
			ModRecipeTypes.capturePristineBaseline(server);
			ModRecipeTypes.setBaselineDirty(false);
			ModRecipeTypes.pushCustomRecipesToServer(server);
			server.getPlayerList().reloadResources();
		});
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) ->
				ModRecipeTypes.pushCustomRecipesToServer(player.server));

		// A fresh install has no forgeconfigapiport.toml yet during onInitialize;
		// by the time a server starts the file exists, so run the per-world flip
		// again then (it takes effect on the next start, as before).
		ServerLifecycleEvents.SERVER_STARTING.register(server -> ensurePerWorldServerConfigs());

		ModBlocks.register();
		ModBlockEntities.register();
		ModTags.register();
		ModItems.register();
		ModCreativeTabs.register();
		ModRecipeTypes.register();
		REGISTRATE.register();

		// Create 6: addon stress values must be registered through BlockStressValues
		BlockStressValues.IMPACTS.registerProvider(block -> {
			if (block == ModBlocks.SIFTER_BLOCK.get())
				return () -> SifterConfig.SIFTER_STRESS_IMPACT.get();
			if (block == ModBlocks.BRASS_SIFTER_BLOCK.get())
				return () -> BrassSifterConfig.BRASS_SIFTER_STRESS_IMPACT.get();
			return null;
		});
	}

	/**
	 * The recipe-edit config is meant to be per world. forgeconfigapiport only
	 * does that when its own switch "forceGlobalServerConfigs" is false, but the
	 * default is true. Flip it once here (marking the file) so every installed
	 * setup gets per-world server configs; if the user later re-enables global
	 * configs on purpose, our marker is present and the choice is respected.
	 */
	private static final String PER_WORLD_MARKER = "# createsifter: per-world server configs enabled";

	private static void ensurePerWorldServerConfigs() {
		try {
			java.nio.file.Path configDir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir();
			java.nio.file.Path fcapi = configDir.resolve("forgeconfigapiport.toml");
			if (!java.nio.file.Files.exists(fcapi))
				return;
			String text = java.nio.file.Files.readString(fcapi);
			boolean hasMarker = text.contains(PER_WORLD_MARKER);
			boolean forceGlobal = text.contains("forceGlobalServerConfigs = true");
			if (forceGlobal && !hasMarker) {
				// API default (or fresh install): switch to per-world and mark it.
				String fixed = text.replaceAll("(?m)^(\\s*forceGlobalServerConfigs\\s*=\\s*)true", "$1false");
				fixed = fixed.replace("forceGlobalServerConfigs = false",
						PER_WORLD_MARKER + System.lineSeparator() + "forceGlobalServerConfigs = false");
				java.nio.file.Files.writeString(fcapi, fixed);
				LOGGER.info("Per-world server configs enabled (forceGlobalServerConfigs=false). Restart once for it to take effect.");
			} else if (!forceGlobal && !hasMarker) {
				// Already per-world but unmarked: record our adoption so a later
				// deliberate "true" by the user is respected.
				String fixed = text.replace("forceGlobalServerConfigs = false",
						PER_WORLD_MARKER + System.lineSeparator() + "forceGlobalServerConfigs = false");
				java.nio.file.Files.writeString(fcapi, fixed);
			}
		} catch (Exception e) {
			LOGGER.warn("Could not enable per-world server configs: {}", e.toString());
		}
	}

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MODID, path);
	}
}

