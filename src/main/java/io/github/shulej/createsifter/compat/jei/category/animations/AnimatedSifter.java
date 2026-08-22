/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.jei.category.animations;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import com.tterrag.registrate.util.entry.BlockEntry;

import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterBlock;
import io.github.shulej.createsifter.register.ModBlocks;
import io.github.shulej.createsifter.register.ModPartials;

public class AnimatedSifter extends BaseAnimatedSifter<SifterBlock> {
	@Override
	PartialModel getMeshModel() {
		return ModPartials.SIFTER_MESH;
	}

	@Override
	PartialModel getCogModel() {
		return ModPartials.SIFTER_COG;
	}

	@Override
	BlockEntry<SifterBlock> getSifterBlock() {
		return ModBlocks.SIFTER_BLOCK;
	}
}
