/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.modmenu;

import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.register.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

/**
 * Runs a server-config write on the thread that owns the config data.
 *
 * In a single-player world the server config is owned by the integrated
 * server thread; writing from the render thread races with its autosave and
 * with the config-change event dispatch. Staging the write through the
 * server's executor keeps every mutation on the same thread. On a dedicated
 * (or LAN-remote) server there is no local config to write and the UI is
 * read-only, so the write just runs in place and is effectively a no-op.
 */
final class ClientConfigWrites {
	private ClientConfigWrites() {}

	static void execute(Runnable write) {
		Minecraft mc = Minecraft.getInstance();
		// Hard guard: never write when no server config is attached to this JVM.
		// Only single-player (integrated server) and the dedicated server itself
		// own the config file; a client connected to a remote server has no local
		// backing config and must never be able to change the server's settings.
		if (!ModConfigs.serverConfigLoaded())
			return;
		Runnable withSave = () -> {
			int tableGenBefore = SifterConfig.recipeTableGeneration();
			write.run();
			ModConfigs.saveServer();
			// Only custom/deleted recipe-table edits change the recipe set; output
			// overrides are applied at roll time and need no re-sync. Reloading the
			// datapacks re-runs the server-side injection and pushes the new table
			// to every player, so hand-sifting / JEI immediately match the change.
			if (SifterConfig.recipeTableGeneration() != tableGenBefore
					&& mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
				MinecraftServer server = mc.getSingleplayerServer();
				server.execute(() -> server.getPlayerList().reloadResources());
			}
		};
		if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
			mc.getSingleplayerServer().execute(withSave);
		} else {
			withSave.run();
		}
	}
}

