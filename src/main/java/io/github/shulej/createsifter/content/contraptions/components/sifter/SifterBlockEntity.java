/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.sifter;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import com.simibubi.create.foundation.item.ItemHelper;

import com.simibubi.create.foundation.sound.SoundScapes;

import net.createmod.catnip.math.VecHelper;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.ViewOnlyWrappedStorageView;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerSlot;
import io.github.shulej.createsifter.ModRecipeTypes;
import io.github.shulej.createsifter.content.contraptions.components.meshes.AdvancedBaseMesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class SifterBlockEntity extends KineticBlockEntity implements SidedStorageBlockEntity {

	public ItemStackHandler inputInv;
	public ItemStackHandler outputInv;
	public ItemStackHandler meshInv;
	public SifterInventoryHandler capability;

	private SiftingRecipe lastRecipe;

	public int timer;

	protected int totalTime;
	protected float minimumSpeed = getDefaultMinimumSpeed();
	protected int itemsProcessedPerCycle = 1;

	/** Ticks to wait before rescanning recipes after a failed search (see tick()). */
	private int recipeSearchCooldown;
	private ItemStack failedMatchInput = ItemStack.EMPTY;
	private ItemStack failedMatchMesh = ItemStack.EMPTY;

	// Negative recipe cache for the insertion path (hoppers / falling items).
	private ItemStack noRecipeInput = ItemStack.EMPTY;
	private ItemStack noRecipeMesh = ItemStack.EMPTY;
	private boolean noRecipeWaterlogged;
	private float noRecipeSpeed;
	private long noRecipeCheckUntil;

	public SifterBlockEntity(BlockEntityType<? extends SifterBlockEntity> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		inputInv = createInputInv();
		outputInv = createOutputInv();
		meshInv = new ItemStackHandler(1){
			@Override
			protected void onContentsChanged(int slot) {
				sendData();
			}

			@Override
			public boolean isItemValid(int slot, ItemVariant resource, int count) {
				return true;
			}
		};
		capability = new SifterInventoryHandler();
	}

	protected ItemStackHandler createInputInv() {
		return new ItemStackHandler(1);
	}

	protected ItemStackHandler createOutputInv() {
		return new ItemStackHandler(SifterConfig.SIFTER_OUTPUT_CAPACITY.get());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void tickAudio() {
		super.tickAudio();

		if (getSpeed() == 0)
			return;
		if (inputInv.getStackInSlot(0).isEmpty())
			return;

		float pitch = Mth.clamp((Math.abs(getSpeed()) / 256f) + .45f, .85f, 1f);
		SoundScapes.play(SoundScapes.AmbienceGroup.MILLING, worldPosition, pitch);
	}

	@Override
	public void tick() {
		super.tick();

		if (getSpeed() == 0) return;
		if (isProcessingPaused()) return;
		if (!isSpeedRequirementFulfilled()) return;

		// Only idle when no output slot can take anything anymore; partial fits are
		// handled by dropping the overflow in process() instead of voiding it.
		boolean outputFull = true;
		for (int i = 0; i < outputInv.getSlotCount(); i++) {
			ItemStack inSlot = outputInv.getStackInSlot(i);
			if (inSlot.getCount() < Math.min(outputInv.getSlotLimit(i), inSlot.getMaxStackSize())) {
				outputFull = false;
				break;
			}
		}
		if (outputFull) return;

		if (timer > 0) {
			timer -= getProcessingSpeed();

			if (level.isClientSide) {
				spawnParticles();
				return;
			}
			if (timer <= 0) {
				process();
			}
			return;
		}

		// The server drives recipe selection and syncs Timer/TotalTime; the client
		// must not fall back to a placeholder loop and desync its progress.
		if (level.isClientSide) return;

		// When the last search found nothing, stay idle for a while instead of
		// rescanning and re-syncing every few ticks. A different input or mesh
		// bypasses the cooldown so freshly inserted items start immediately.
		boolean sameFailure = recipeSearchCooldown > 0
				&& ItemStack.isSameItemSameTags(inputInv.getStackInSlot(0), failedMatchInput)
				&& ItemStack.isSameItemSameTags(meshInv.getStackInSlot(0), failedMatchMesh);
		if (recipeSearchCooldown > 0 && sameFailure) {
			recipeSearchCooldown--;
			return;
		}
		recipeSearchCooldown = 0;

		if (inputInv.getStackInSlot(0).isEmpty()) return;

		ItemStackHandlerContainer inventoryIn = new ItemStackHandlerContainer(2);
		inventoryIn.setStackInSlot(0, this.inputInv.getStackInSlot(0));
		inventoryIn.setStackInSlot(1, this.meshInv.getStackInSlot(0));

		if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh())) {
			Optional<SiftingRecipe> recipe = ModRecipeTypes.SIFTING.find(inventoryIn, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh());
			if (!recipe.isPresent()) {
				lastRecipe = null;
				failedMatchInput = this.inputInv.getStackInSlot(0).copy();
				failedMatchMesh = this.meshInv.getStackInSlot(0).copy();
				recipeSearchCooldown = 100;
				boolean changed = timer != 0 || totalTime != 0;
				timer = 0;
				totalTime = 0;
				minimumSpeed = getDefaultMinimumSpeed();
				if (changed) sendData();
				return;
			}
			lastRecipe = recipe.get();
		}
		timer = lastRecipe.getProcessingDuration();
		totalTime = timer;
		minimumSpeed = lastRecipe.getSpeedRequirement();

		sendData();
	}

	public void spawnParticles() {
		if (inputInv.getStackInSlot(0).isEmpty() || meshInv.getStackInSlot(0).isEmpty())
			return;

		ItemParticleOption data = new ItemParticleOption(ParticleTypes.ITEM, inputInv.getStackInSlot(0));
		float angle = level.random.nextFloat() * 360;
		Vec3 offset = new Vec3(0, 0, 0.5f);
		offset = VecHelper.rotate(offset, angle, Direction.Axis.Y);
		Vec3 target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Direction.Axis.Y);

		Vec3 center = offset.add(VecHelper.getCenterOf(worldPosition));
		target = VecHelper.offsetRandomly(target.subtract(offset), level.random, 1 / 128f);
		level.addParticle(data, center.x, center.y, center.z, target.x, target.y, target.z);
	}


	private void process() {
		if (inputInv.getStackInSlot(0).isEmpty())
			return;

		ItemStackHandlerContainer inventoryIn = new ItemStackHandlerContainer(2);
		inventoryIn.setStackInSlot(0, this.inputInv.getStackInSlot(0));
		inventoryIn.setStackInSlot(1, this.meshInv.getStackInSlot(0));
		if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh())) {
			Optional<SiftingRecipe> recipe = ModRecipeTypes.SIFTING.find(inventoryIn, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh());
			if (!recipe.isPresent())
				return;
			lastRecipe = recipe.get();
		}
		List<ItemStack> overflow = new ArrayList<>();
		for (int i = 0; i < getItemsProcessedPerCycle(); i++) {
			try (Transaction t = TransferUtil.getTransaction()) {
				ItemStackHandlerSlot slot = inputInv.getSlot(0);
				if (slot.isResourceBlank())
					break;
				slot.extract(slot.getResource(), 1, t);

				for (ItemStack stack : lastRecipe.rollResults())
					overflow.add(tryToInsertOutputItem(outputInv, stack, t));
				t.commit();
			}
		}
		// Anything that did not fit is dropped above the sifter instead of being deleted.
		for (ItemStack leftover : overflow)
			if (!leftover.isEmpty())
				Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
						worldPosition.getZ() + 0.5, leftover);

		sendData();
		setChanged();
	}

	/** @return the part of the stack that did not fit (dropped by the caller); EMPTY when fully stored. */
	protected ItemStack tryToInsertOutputItem(ItemStackHandler inv, ItemStack stack, Transaction t) {
		long inserted = inv.insert(ItemVariant.of(stack), stack.getCount(), t);
		long leftover = stack.getCount() - inserted;
		if (leftover <= 0)
			return ItemStack.EMPTY;
		ItemStack rest = stack.copy();
		rest.setCount((int) leftover);
		return rest;
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("Timer", timer);
		compound.put("MeshInventory", meshInv.serializeNBT());
		compound.put("InputInventory", inputInv.serializeNBT());
		compound.put("OutputInventory", outputInv.serializeNBT());
		compound.putInt("TotalTime", totalTime);
		compound.putFloat("MinimumSpeed", minimumSpeed);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		timer = compound.getInt("Timer");
		meshInv.deserializeNBT(compound.getCompound("MeshInventory"));
		inputInv.deserializeNBT(compound.getCompound("InputInventory"));
		outputInv.deserializeNBT(compound.getCompound("OutputInventory"));
		totalTime = compound.getInt("TotalTime");
		minimumSpeed = compound.getFloat("MinimumSpeed");
		super.read(compound, clientPacket);
	}

	protected int getItemsProcessedPerCycle() {
		return itemsProcessedPerCycle;
	}

	@Nullable
	@Override
	public Storage<ItemVariant> getItemStorage(@Nullable Direction direction) {
		return capability;
	}

	/**
	 * Place one mesh into the mesh slot, consuming one from the held stack.
	 * @return true when a mesh was actually placed (slot was empty).
	 */
	public boolean insertMesh(ItemStack meshStack, Player player) {
		if (meshInv.getStackInSlot(0).isEmpty()) {
			ItemStack meshToInsert = meshStack.copy();
			meshToInsert.setCount(1);
			meshStack.shrink(1);
			meshInv.setStackInSlot(0, meshToInsert);
			setChanged();
			return true;
		}
		return false;
	}

	public boolean hasMesh(){
		return !meshInv.getStackInSlot(0).isEmpty();
	}

	public void removeMesh(Player player) {
		player.getInventory().placeItemBackInInventory(meshInv.getStackInSlot(0));
		meshInv.setStackInSlot(0, ItemStack.EMPTY);
		timer = 0;
		totalTime = 0;
		minimumSpeed = getDefaultMinimumSpeed();
		sendData();
		setChanged();
	}

	@Override
	public boolean isSpeedRequirementFulfilled() {
		return getAbsSpeed() >= minimumSpeed;
	}

	public float getAbsSpeed() {
		return Math.abs(getSpeed());
	}

	public int getProcessingSpeed() {
		return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
	}

	public ItemStack getInputItemStack(){
		return this.inputInv.getStackInSlot(0);
	}

	/** Remaining fraction of the current cycle, from 1 down to 0; never 0/NaN so renderers can divide by it. */
	public float getProcessingRemainingPercent() {
		if (totalTime <= 0)
			return 1;
		return Mth.clamp(timer / (float) totalTime, 0.05f, 1f);
	}

	private boolean canProcess(ItemStack stack) {
		ItemStack mesh = this.meshInv.getStackInSlot(0);
		long now = level != null ? level.getGameTime() : 0;

		// Negative cache: an un-siftable (input, mesh) combination is checked at
		// most once per second so hopper/falling-item inserts don't rescan every tick.
		if (now < noRecipeCheckUntil
				&& ItemStack.isSameItemSameTags(stack, noRecipeInput)
				&& ItemStack.isSameItemSameTags(mesh, noRecipeMesh)
				&& noRecipeWaterlogged == isWaterLogged()
				&& noRecipeSpeed == getAbsSpeed())
			return false;

		ItemStackHandlerContainer tester = new ItemStackHandlerContainer(2);
		tester.setStackInSlot(0, stack);
		tester.setStackInSlot(1, mesh);

		boolean matches = (lastRecipe != null && lastRecipe.matches(tester, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh()))
				|| ModRecipeTypes.SIFTING.find(tester, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh()).isPresent();
		if (!matches) {
			noRecipeInput = stack.copy();
			noRecipeMesh = mesh.copy();
			noRecipeWaterlogged = isWaterLogged();
			noRecipeSpeed = getAbsSpeed();
			noRecipeCheckUntil = now + 20;
		}
		return matches;
	}

	/** Pauses processing (e.g. a redstone lock) without stopping kinetics or behaviours. */
	protected boolean isProcessingPaused() {
		return false;
	}

	public boolean isWaterLogged() {
		return this.getBlockState().getValue(BlockStateProperties.WATERLOGGED);
	}

	public boolean hasAdvancedMesh(){
		return !meshInv.getStackInSlot(0).isEmpty() && meshInv.getStackInSlot(0).getItem() instanceof AdvancedBaseMesh;
	}

	private class SifterInventoryHandler extends CombinedStorage<ItemVariant, ItemStackHandler> {
		public SifterInventoryHandler() {
			super(List.of(inputInv, outputInv));
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext context) {
			if (canProcess(resource.toStack()))
				return inputInv.insert(resource, maxAmount, context);
			return 0;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext context) {
			return outputInv.extract(resource, maxAmount, context);
		}

		@Override
		public @NotNull Iterator<StorageView<ItemVariant>> iterator() {
			return new SifterInventoryHandlerIterator();
		}

		private class SifterInventoryHandlerIterator implements Iterator<StorageView<ItemVariant>> {
			private boolean output = true;
			private Iterator<StorageView<ItemVariant>> wrapped;

			public SifterInventoryHandlerIterator() {
				wrapped = outputInv.iterator();
			}

			@Override
			public boolean hasNext() {
				return wrapped.hasNext();
			}

			@Override
			public StorageView<ItemVariant> next() {
				StorageView<ItemVariant> view = wrapped.next();
				if (!output) view = new ViewOnlyWrappedStorageView<>(view);
				if (output && !hasNext()) {
					wrapped = inputInv.iterator();
					output = false;
				}
				return view;
			}
		}
	}

	public float getProgress() {
		return timer;
	}

	@Override
	public void invalidate() {
		super.invalidate();
	}

	@Override
	public void destroy() {
		super.destroy();
		ItemHelper.dropContents(level, worldPosition, inputInv);
		ItemHelper.dropContents(level, worldPosition, meshInv);
		ItemHelper.dropContents(level, worldPosition, outputInv);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = new CompoundTag();
		this.write(nbt, true);
		return nbt;
	}

	protected float getDefaultMinimumSpeed() {
		return SifterConfig.SIFTER_MINIMUM_SPEED.get().floatValue();
	}
}

