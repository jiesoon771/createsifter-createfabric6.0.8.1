/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the MIT License.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.register;

import io.github.shulej.createsifter.CreateSifter;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class ModCreativeTabs {
	public static final CreativeModeTab MAIN = FabricItemGroup.builder().icon(ModCreativeTabs::makeIcon).title(Component.translatable("itemGroup.createsifter:main")).build();
	public static final ResourceKey<CreativeModeTab> MAIN_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(CreateSifter.MODID, "main"));
	public static ItemStack makeIcon() {
		return new ItemStack(ModBlocks.SIFTER_BLOCK);
	}

	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MAIN_KEY, MAIN);
		ItemGroupEvents.modifyEntriesEvent(MAIN_KEY).register(content -> {
			CreateSifter.REGISTRATE.getAll(Registries.ITEM).forEach(entry -> {
				content.accept(entry.get());
			});
		});
	}
}

