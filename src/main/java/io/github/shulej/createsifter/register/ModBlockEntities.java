/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.register;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import io.github.shulej.createsifter.CreateSifter;
import io.github.shulej.createsifter.content.contraptions.components.brass_sifter.BrassSifterBlockEntity;
import io.github.shulej.createsifter.content.contraptions.components.brass_sifter.BrassSifterRenderer;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterBlockEntity;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterRenderer;

public class ModBlockEntities {
	public static final BlockEntityEntry<SifterBlockEntity> SIFTER = CreateSifter.REGISTRATE
			.blockEntity("sifter", SifterBlockEntity::new)
			.visual(() -> SingleAxisRotatingVisual.of(ModPartials.SIFTER_COG))
			.validBlocks(ModBlocks.SIFTER_BLOCK)
			.renderer(() -> SifterRenderer::new)
			.register();

	public static final BlockEntityEntry<BrassSifterBlockEntity> BRASS_SIFTER = CreateSifter.REGISTRATE
			.blockEntity("brass_sifter", BrassSifterBlockEntity::new)
			.visual(() -> SingleAxisRotatingVisual.of(ModPartials.BRASS_SIFTER_COG))
			.validBlocks(ModBlocks.BRASS_SIFTER_BLOCK)
			.renderer(() -> BrassSifterRenderer::new)
			.register();

	public static void register() {

	}
}

