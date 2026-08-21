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
	public void tick() {
		if (getBlockState().getOptionalValue(BlockStateProperties.POWERED)
				.orElse(false))
			return;
		super.tick();
	}

	@Override
	protected ItemStack tryToInsertOutputItem(ItemStackHandler outputInv, ItemStack stack, Transaction t) {
		// Outputs the filter rejects are intentionally discarded; anything the filter
		// accepts but that no longer fits is returned and dropped by the base class.
		if (filtering != null && !filtering.test(stack)) {
			return ItemStack.EMPTY;
		}
		return super.tryToInsertOutputItem(outputInv, stack, t);
	}

	@Override
	protected float getDefaultMinimumSpeed() {
		return BrassSifterConfig.BRASS_SIFTER_MINIMUM_SPEED.get().floatValue();
	}
}
