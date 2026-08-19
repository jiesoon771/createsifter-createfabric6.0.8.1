package io.github.shulej.createsifter.content.contraptions.components.brass_sifter;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.createmod.catnip.math.VecHelper;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BrassSifterFilterSlotPosition extends ValueBoxTransform.Sided {
	@Override
	protected boolean isSideActive(BlockState state, Direction direction) {
		return direction != Direction.UP && direction != Direction.DOWN;
	}

	@Override
	protected Vec3 getSouthLocation() {
		return VecHelper.voxelSpace(8f, 13f, 16f);
	}
}
