/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.brass_sifter;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class BrassSifterBlockEntity extends SifterBlockEntity {
	private FilteringBehaviour filtering;

	public BrassSifterBlockEntity(BlockEntityType<? extends SifterBlockEntity> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	@Override
	protected ItemStackHandler createOutputInv() {
		return new ItemStackHandler(BrassSifterConfig.BRASS_SIFTER_OUTPUT_CAPACITY.get());
	}

	@Override
	protected int getItemsProcessedPerCycle() {
		return BrassSifterConfig.BRASS_SIFTER_ITEMS_PER_CYCLE.get();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);

		filtering = new FilteringBehaviour(this, new BrassSifterFilterSlotPosition()).forRecipes();
		behaviours.add(filtering);
	}

	@Override
	protected boolean isProcessingPaused() {
		// Redstone locks only pause sifting; kinetics and behaviours keep ticking.
		return getBlockState().getOptionalValue(BlockStateProperties.POWERED)
				.orElse(false);
	}

	@Override
	protected ItemStack tryToInsertOutputItem(ItemStackHandler outputInv, ItemStack stack, Transaction t) {
		// Outputs the filter rejects are handed back so the base class drops them
		// in the world instead of silently voiding them.
		if (filtering != null && !filtering.test(stack)) {
			return stack;
		}
		return super.tryToInsertOutputItem(outputInv, stack, t);
	}

	@Override
	protected float getDefaultMinimumSpeed() {
		return BrassSifterConfig.BRASS_SIFTER_MINIMUM_SPEED.get().floatValue();
	}
}

