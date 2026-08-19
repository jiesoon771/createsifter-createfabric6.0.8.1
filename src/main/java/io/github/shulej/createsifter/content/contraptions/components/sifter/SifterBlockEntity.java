package io.github.shulej.createsifter.content.contraptions.components.sifter;

import com.google.gson.Gson;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class SifterBlockEntity extends KineticBlockEntity implements SidedStorageBlockEntity {
	private static final Logger log = LoggerFactory.getLogger(SifterBlockEntity.class);
	public static float DEFAULT_MINIMUM_SPEED = SifterConfig.SIFTER_MINIMUM_SPEED.get().floatValue();

	public ItemStackHandler inputInv;
	public ItemStackHandler outputInv;
	public ItemStackHandler meshInv;
	public SifterInventoryHandler capability;

	private SiftingRecipe lastRecipe;

	public int timer;

	protected int totalTime;
	protected float minimumSpeed = getDefaultMinimumSpeed();
	protected int itemsProcessedPerCycle = 1;

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
		if (!isSpeedRequirementFulfilled()) return;

		for (int i = 0; i < outputInv.getSlotCount(); i++) {
			if (outputInv.getStackInSlot(i).getCount() == outputInv.getSlotLimit(i)) return;
		}

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

		ItemStackHandlerContainer inventoryIn = new ItemStackHandlerContainer(2);
		inventoryIn.setStackInSlot(0, this.inputInv.getStackInSlot(0));
		inventoryIn.setStackInSlot(1, this.meshInv.getStackInSlot(0));

		if (inputInv.getStackInSlot(0).isEmpty()) return;

		if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh())) {
			Optional<SiftingRecipe> recipe = ModRecipeTypes.SIFTING.find(inventoryIn, level, this.isWaterLogged(), getAbsSpeed());
			if (!recipe.isPresent()) {
				timer = 100;
				totalTime = 100;
				minimumSpeed = getDefaultMinimumSpeed();
				sendData();
			} else {
				lastRecipe = recipe.get();
				timer = lastRecipe.getProcessingDuration();
				totalTime = lastRecipe.getProcessingDuration();
				minimumSpeed = lastRecipe.getSpeedRequirement();
				sendData();
			}
			return;
		}
		timer = lastRecipe.getProcessingDuration();
		totalTime = lastRecipe.getProcessingDuration();
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
		ItemStackHandlerContainer inventoryIn = new ItemStackHandlerContainer(2);
		inventoryIn.setStackInSlot(0, this.inputInv.getStackInSlot(0));
		inventoryIn.setStackInSlot(1, this.meshInv.getStackInSlot(0));
		if (lastRecipe == null || !lastRecipe.matches(inventoryIn, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh())) {
			Optional<SiftingRecipe> recipe = ModRecipeTypes.SIFTING.find(inventoryIn, level, this.isWaterLogged(), getAbsSpeed());
			if (!recipe.isPresent())
				return;
			lastRecipe = recipe.get();
		}
		for (int i = 0; i < getItemsProcessedPerCycle(); i++) {
			try (Transaction t = TransferUtil.getTransaction()) {
				ItemStackHandlerSlot slot = inputInv.getSlot(0);
				if (slot.isResourceBlank())
					break;
				slot.extract(slot.getResource(), 1, t);

				lastRecipe.rollResults().forEach(stack -> tryToInsertOutputItem(outputInv, stack, t));
				t.commit();
			}
		}

		sendData();
		setChanged();
	}

	protected void tryToInsertOutputItem(ItemStackHandler inv, ItemStack stack, Transaction t) {
		inv.insert(ItemVariant.of(stack), stack.getCount(), t);
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

	public void insertMesh(ItemStack meshStack, Player player) {
		if (meshInv.getStackInSlot(0).isEmpty()) {
			ItemStack meshToInsert = meshStack.copy();
			meshToInsert.setCount(1);
			meshStack.shrink(1);
			meshInv.setStackInSlot(0, meshToInsert);
			setChanged();
		}
	}

	public boolean hasMesh(){
		return !meshInv.getStackInSlot(0).isEmpty();
	}

	public void removeMesh(Player player) {
		player.getInventory().placeItemBackInInventory(meshInv.getStackInSlot(0));
		meshInv.setStackInSlot(0, ItemStack.EMPTY);
		timer = 100;
		totalTime = 100;
		minimumSpeed = getDefaultMinimumSpeed();
		sendData();
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

	public float getProcessingRemainingPercent() {
		float timer = this.timer;
		float total = this.totalTime;
		float remaining = total - timer;
		float result = remaining/total;
		return 1 - result;
	}

	private boolean canProcess(ItemStack stack) {
		ItemStackHandlerContainer tester = new ItemStackHandlerContainer(2);
		tester.setStackInSlot(0, stack);
		tester.setStackInSlot(1, this.meshInv.getStackInSlot(0));

		if (lastRecipe != null && lastRecipe.matches(tester, level, this.isWaterLogged(), getAbsSpeed(), hasAdvancedMesh())) {
			return true;
		}
		return ModRecipeTypes.SIFTING.find(tester, level, this.isWaterLogged(), getAbsSpeed()).isPresent();
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
		return DEFAULT_MINIMUM_SPEED;
	}
}
