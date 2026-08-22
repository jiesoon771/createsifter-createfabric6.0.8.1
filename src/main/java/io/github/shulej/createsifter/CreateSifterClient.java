/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter;

import io.github.shulej.createsifter.content.contraptions.components.meshes.MeshItemRenderer;
import io.github.shulej.createsifter.register.ModPartials;
import io.github.shulej.createsifter.register.ModPonders;
import net.createmod.ponder.foundation.PonderIndex;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;

public class CreateSifterClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ModPartials.init();
		PonderIndex.addPlugin(new ModPonders());
	}

	public static void registerItem(@Nonnull ItemLike item) {
		BuiltinItemRendererRegistry.INSTANCE.register(item, new MeshItemRenderer());
	}
}

