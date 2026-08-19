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
