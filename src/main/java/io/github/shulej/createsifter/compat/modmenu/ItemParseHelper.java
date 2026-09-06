/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.modmenu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Resolves free-text item input (registry id or in-game display name) to an
 * ItemStack, the same way the creative search bar accepts both forms.
 *
 * 把玩家输入（物品 ID 或游戏内名称）解析为物品，与创造模式搜索栏的输入方式一致。
 */
public final class ItemParseHelper {
	private ItemParseHelper() {}

	/**
	 * @param text        trimmed player input; a registry id like "minecraft:gravel"
	 *                    or an exact display name like "沙砾".
	 * @param requireBlock when true, only block items (siftables) are accepted.
	 * @return the matched item, or {@link ItemStack#EMPTY} when nothing matches.
	 */
	public static ItemStack parse(String text, boolean requireBlock) {
		String t = text == null ? "" : text.trim();
		if (t.isEmpty())
			return ItemStack.EMPTY;

		// 1) registry id: minecraft:gravel
		if (ResourceLocation.isValidResourceLocation(t)) {
			ResourceLocation id = ResourceLocation.tryParse(t);
			if (id != null) {
				Item item = BuiltInRegistries.ITEM.get(id);
				if (item != Items.AIR) {
					ItemStack stack = new ItemStack(item);
					if (!requireBlock || stack.getItem() instanceof BlockItem)
						return stack;
				}
			}
		}

		// 2) exact display name (case-insensitive, current client language)
		for (Item item : BuiltInRegistries.ITEM) {
			ItemStack stack = new ItemStack(item);
			if (stack.isEmpty())
				continue;
			if (stack.getHoverName().getString().equalsIgnoreCase(t)) {
				if (!requireBlock || stack.getItem() instanceof BlockItem)
					return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Outcome of {@link #parseBlockRequired}: the resolved block item, or
	 * {@code foundNonBlock} set when a matching item exists but is not a block —
	 * the UI shows a dedicated hint in that case so players understand why the
	 * input was rejected.
	 */
	public record BlockResult(ItemStack stack, boolean foundNonBlock) {
	}

	/**
	 * Block-required parse used by the new-recipe siftable input. Same lookup
	 * rules as {@link #parse(String, boolean)} with {@code requireBlock = true},
	 * but it distinguishes "no such item" from "found, but not a block".
	 */
	public static BlockResult parseBlockRequired(String text) {
		String t = text == null ? "" : text.trim();
		if (t.isEmpty())
			return new BlockResult(ItemStack.EMPTY, false);

		boolean foundNonBlock = false;
		// 1) registry id: minecraft:gravel
		if (ResourceLocation.isValidResourceLocation(t)) {
			ResourceLocation id = ResourceLocation.tryParse(t);
			if (id != null) {
				Item item = BuiltInRegistries.ITEM.get(id);
				if (item != Items.AIR) {
					ItemStack stack = new ItemStack(item);
					if (stack.getItem() instanceof BlockItem)
						return new BlockResult(stack, false);
					foundNonBlock = true;
				}
			}
		}

		// 2) exact display name (case-insensitive, current client language)
		for (Item item : BuiltInRegistries.ITEM) {
			ItemStack stack = new ItemStack(item);
			if (stack.isEmpty())
				continue;
			if (stack.getHoverName().getString().equalsIgnoreCase(t)) {
				if (stack.getItem() instanceof BlockItem)
					return new BlockResult(stack, false);
				foundNonBlock = true;
			}
		}
		return new BlockResult(ItemStack.EMPTY, foundNonBlock);
	}
}
