/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.meshes;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.CustomUseEffectsItem;

import com.simibubi.create.foundation.mixin.accessor.LivingEntityAccessor;
import net.createmod.catnip.math.VecHelper;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import io.github.shulej.createsifter.CreateSifterClient;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import net.createmod.catnip.data.TriState;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BaseMesh extends Item implements CustomUseEffectsItem {
	protected MeshTypes mesh;
	public BaseMesh(Properties pProperties) {
		super(pProperties);
		EnvExecutor.runWhenOn(EnvType.CLIENT, () -> {
			return () -> {
				CreateSifterClient.registerItem(this);
			};
		});
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack itemStack = playerIn.getItemInHand(handIn);
		InteractionResultHolder<ItemStack> FAIL = new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);

		if (itemStack.getOrCreateTag().contains("Sifting")) {
			playerIn.startUsingItem(handIn);
			return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
		}

		InteractionHand otherHand = handIn == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack itemInOtherHand = playerIn.getItemInHand(otherHand);

		Block blockUnderPlayer = playerIn.getFeetBlockState().getBlock();
		boolean waterlogged = blockUnderPlayer instanceof LiquidBlock;

		if (SiftingRecipe.canHandSift(worldIn, itemInOtherHand, itemStack, waterlogged)) {
			ItemStack item = itemInOtherHand.copy();
			ItemStack toSift = item.split(1);
			playerIn.startUsingItem(handIn);
			itemStack.getOrCreateTag().put("Sifting", toSift.serializeNBT());
			playerIn.setItemInHand(otherHand, item);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
		}

		HitResult raytraceresult = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.NONE);
		if (!(raytraceresult instanceof BlockHitResult))
			return FAIL;
		BlockHitResult ray = (BlockHitResult) raytraceresult;
		Vec3 hitVec = ray.getLocation();

		AABB bb = new AABB(hitVec, hitVec).inflate(1f);
		ItemEntity pickUp = null;
		for (ItemEntity itemEntity : worldIn.getEntitiesOfClass(ItemEntity.class, bb)) {
			if (!itemEntity.isAlive())
				continue;
			if (itemEntity.position().distanceTo(playerIn.position()) > 3)
				continue;
			ItemStack stack = itemEntity.getItem();
			if (!SiftingRecipe.canHandSift(worldIn, stack, itemStack, waterlogged))
				continue;
			pickUp = itemEntity;
			break;
		}

		if (pickUp == null)
			return FAIL;

		ItemStack item = pickUp.getItem().copy();
		ItemStack toSift = item.split(1);

		playerIn.startUsingItem(handIn);

		// Write the tag on both sides: the client needs it immediately for the
		// hold-out animation and particles (see MeshItemRenderer), the server needs
		// it to roll the recipe at finishUsingItem time.
		itemStack.getOrCreateTag().put("Sifting", toSift.serializeNBT());
		if (!worldIn.isClientSide) {
			if (item.isEmpty())
				pickUp.discard();
			else
				pickUp.setItem(item);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
		if (!(entityLiving instanceof Player))
			return stack;
		Player player = (Player) entityLiving;
		CompoundTag tag = stack.getOrCreateTag();
		Block blockUnderPlayer = player.getFeetBlockState().getBlock();
		boolean waterlogged = blockUnderPlayer instanceof LiquidBlock;
		if (tag.contains("Sifting")) {
			ItemStack toSift = ItemStack.of(tag.getCompound("Sifting"));

			// The client only drives the visuals; the server is the sole authority
			// on the roll. Rolling here too would double-consume the RNG stream and
			// produce items the client never gives to anyone.
			if (worldIn.isClientSide) {
				spawnParticles(entityLiving.getEyePosition(1).add(entityLiving.getLookAngle().scale(.5f)), toSift, worldIn);
				return stack;
			}

			List<ItemStack> sifted = SiftingRecipe.applyHandSift(worldIn, entityLiving.position(), toSift, stack, waterlogged);

			if (!sifted.isEmpty()) {
				sifted.forEach(outputStack -> {
					if (player instanceof FakePlayer) {
						player.drop(outputStack, false, false);
					} else {
						player.getInventory().placeItemBackInInventory(outputStack);
					}
				});
			}
			tag.remove("Sifting");
			stack.hurtAndBreak(1, entityLiving, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
		}

		return stack;
	}

	public static void spawnParticles(Vec3 location, ItemStack polishedStack, Level world) {
		for (int i = 0; i < 20; i++) {
			Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, world.random, 1 / 8f);
			world.addParticle(new ItemParticleOption(ParticleTypes.ITEM, polishedStack), location.x, location.y,
					location.z, motion.x, motion.y, motion.z);
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
		if (!(entityLiving instanceof Player))
			return;
		Player player = (Player) entityLiving;
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("Sifting")) {
			ItemStack toSift = ItemStack.of(tag.getCompound("Sifting"));
			player.getInventory()
					.placeItemBackInInventory(toSift);
			tag.remove("Sifting");
		}
	}

	@Override
	public TriState shouldTriggerUseEffects(ItemStack stack, LivingEntity entity) {
		// Trigger every tick so that we have more fine grain control over the animation
		return TriState.TRUE;
	}

	@Override
	public boolean triggerUseEffects(ItemStack stack, LivingEntity entity, int i, RandomSource random) {
		CompoundTag tag = stack.getOrCreateTag();
		if (tag.contains("Sifting")) {
			ItemStack sifting = ItemStack.of(tag.getCompound("Sifting"));
			((LivingEntityAccessor) entity).create$callSpawnItemParticles(sifting, 1);
		}

		// After 6 ticks play the sound every 7th
		if ((entity.getTicksUsingItem() - 6) % 7 == 0)
			entity.playSound(entity.getEatingSound(stack), 0.9F + 0.2F * random.nextFloat(),
					random.nextFloat() * 0.2F + 0.9F);
		return true;
	}

	@Override
	public SoundEvent getEatingSound() {
		return AllSoundEvents.SANDING_SHORT.getMainEvent();
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.EAT;
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}
}
