/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the MIT License.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.sifter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

/**
 * Shared right-click handling for the two sifter variants.
 *
 * The client mirrors the server's verdict: clicks with a non-mesh item pass
 * through so the item's own use (block placement, hand-sifting, ...) still
 * triggers, while clicks the server will consume are claimed here. A successful
 * mesh insertion short-circuits so it never falls into the empty-hand pickup
 * branch (relevant since meshes are single-use-stack items).
 */
public final class SifterInteractionHelper {
	private SifterInteractionHelper() {}

	public static InteractionResult useSifter(Level worldIn, BlockPos pos, Player player, InteractionHand handIn, Predicate<Item> meshItem) {
		if (!(worldIn.getBlockEntity(pos) instanceof SifterBlockEntity sifterBlockEntity))
			return InteractionResult.PASS;
		ItemStack handInStack = player.getItemInHand(handIn);

		if (worldIn.isClientSide) {
			if (!handInStack.isEmpty() && !meshItem.test(handInStack.getItem()))
				return InteractionResult.PASS;
			return InteractionResult.SUCCESS;
		}

		if (meshItem.test(handInStack.getItem())) {
			if (sifterBlockEntity.insertMesh(handInStack, player))
				return InteractionResult.SUCCESS;
		}

		if (!handInStack.isEmpty())
			return InteractionResult.PASS;

		boolean emptyOutput = true;
		if (handInStack.isEmpty() && sifterBlockEntity.hasMesh() && player.isShiftKeyDown()) {
			sifterBlockEntity.removeMesh(player);
		}

		for (int slot = 0; slot < sifterBlockEntity.outputInv.getSlotCount(); slot++) {
			ItemStack itemInSlot = sifterBlockEntity.outputInv.getStackInSlot(slot);
			if (!itemInSlot.isEmpty())
				emptyOutput = false;
			player.getInventory().placeItemBackInInventory(itemInSlot);
			sifterBlockEntity.outputInv.setStackInSlot(slot, ItemStack.EMPTY);
		}

		if (emptyOutput) {
			for (int slot = 0; slot < sifterBlockEntity.inputInv.getSlotCount(); slot++) {
				player.getInventory().placeItemBackInInventory(sifterBlockEntity.inputInv.getStackInSlot(slot));
				sifterBlockEntity.inputInv.setStackInSlot(slot, ItemStack.EMPTY);
			}
		}

		sifterBlockEntity.setChanged();
		sifterBlockEntity.sendData();

		return InteractionResult.SUCCESS;
	}
}

