/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the MIT License.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.sifter;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

import com.simibubi.create.foundation.block.IBE;

import net.createmod.catnip.data.Iterate;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.shulej.createsifter.content.contraptions.components.meshes.BaseMesh;
import io.github.shulej.createsifter.register.ModBlockEntities;
import io.github.shulej.createsifter.register.ModShapes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SifterBlock extends KineticBlock implements IBE<SifterBlockEntity>, ICogWheel, SimpleWaterloggedBlock {
	public SifterBlock(Properties properties) {
		super(properties);
		this.registerDefaultState((BlockState) this.getStateDefinition().any().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		return SifterInteractionHelper.useSifter(worldIn, pos, player, handIn, item -> item instanceof BaseMesh);
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn) {
		super.updateEntityAfterFallOn(worldIn, entityIn);

		if (entityIn.level().isClientSide)
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!entityIn.isAlive())
			return;

		SifterBlockEntity sifter = null;
		for (BlockPos pos : Iterate.hereAndBelow(entityIn.blockPosition()))
			if (sifter == null)
				sifter = getBlockEntity(worldIn, pos);

		if (sifter == null)
			return;

		ItemEntity itemEntity = (ItemEntity) entityIn;
		Storage<ItemVariant> capability = sifter.getItemStorage(null);
		if (capability == null)
			return;

		try (Transaction t = TransferUtil.getTransaction()) {
			ItemStack inEntity = itemEntity.getItem();
			long inserted = capability.insert(ItemVariant.of(inEntity), inEntity.getCount(), t);
			if (inserted == inEntity.getCount())
				itemEntity.discard();
			else itemEntity.setItem(ItemHandlerHelper.copyStackWithSize(inEntity, (int) (inEntity.getCount() - inserted)));
			t.commit();
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ModShapes.SIFTER;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return Direction.Axis.Y;
	}

	@Override
	public Class<SifterBlockEntity> getBlockEntityClass() {
		return SifterBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends SifterBlockEntity> getBlockEntityType() {
		return ModBlockEntities.SIFTER.get();
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState FluidState = context.getLevel().getFluidState(context.getClickedPos());
		return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED, FluidState.getType() == Fluids.WATER);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.WATERLOGGED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	public SpeedLevel getMinimumRequiredSpeedLevel() {
		return super.getMinimumRequiredSpeedLevel();
	}
}

