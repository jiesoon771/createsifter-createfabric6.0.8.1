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

	public static ResourceLocation asResource(String path) {
		return new ResourceLocation(MODID, path);
	}
}
