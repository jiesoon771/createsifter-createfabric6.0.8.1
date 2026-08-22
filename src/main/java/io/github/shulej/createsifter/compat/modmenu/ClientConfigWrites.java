/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the MIT License.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.modmenu;

import io.github.shulej.createsifter.register.ModConfigs;
import net.minecraft.client.Minecraft;

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
		Runnable withSave = () -> {
			write.run();
			ModConfigs.saveServer();
		};
		if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
			mc.getSingleplayerServer().execute(withSave);
		} else {
			withSave.run();
		}
	}
}

