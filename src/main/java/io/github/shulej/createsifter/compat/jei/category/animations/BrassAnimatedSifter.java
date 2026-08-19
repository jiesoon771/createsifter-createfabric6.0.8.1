package io.github.shulej.createsifter.compat.jei.category.animations;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

import com.tterrag.registrate.util.entry.BlockEntry;

import io.github.shulej.createsifter.content.contraptions.components.brass_sifter.BrassSifterBlock;
import io.github.shulej.createsifter.register.ModBlocks;
import io.github.shulej.createsifter.register.ModPartials;

public class BrassAnimatedSifter extends BaseAnimatedSifter<BrassSifterBlock> {
	@Override
	PartialModel getMeshModel() {
		return ModPartials.BRASS_SIFTER_MESH;
	}

	@Override
	PartialModel getCogModel() {
		return ModPartials.BRASS_SIFTER_COG;
	}

	@Override
	BlockEntry<BrassSifterBlock> getSifterBlock() {
		return ModBlocks.BRASS_SIFTER_BLOCK;
	}
}
