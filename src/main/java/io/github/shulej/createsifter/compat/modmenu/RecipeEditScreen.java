/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.modmenu;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import io.github.shulej.createsifter.foundation.data.recipe.CustomRecipeStore;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Per-recipe editor. For each output of a sifting recipe the player can change
 * the drop chance (0-100%) and the amount per success, delete single outputs
 * (red X), append new outputs through an item input row, or delete the whole
 * recipe (with confirmation). Every change is staged in memory and written to
 * the config table when the screen closes (single-player only).
 *
 * 单条配方的编辑界面：逐条修改产物概率/数量、红叉删除单个产物、输入行追加新产物、
 * 或（确认后）删除整个配方。改动暂存内存，关闭界面时统一提交到配置。
 */
public class RecipeEditScreen extends SifterRecipeEditorScreen {

	private final SiftingRecipe recipe;

	public RecipeEditScreen(Screen parent, SiftingRecipe recipe) {
		super(Component.translatable("createsifter.config.recipe_title"), parent);
		this.recipe = recipe;
		List<ProcessingOutput> outputs = recipe.getRollableResults();
		for (int i = 0; i < outputs.size(); i++) {
			ProcessingOutput out = outputs.get(i);
			int[] ov = SifterConfig.getOverride(recipe.getId().toString(), i);
			int chance = ov != null ? ov[0] : Math.round(out.getChance() * 100);
			int count = ov != null ? ov[1] : out.getStack().getCount();
			rows.add(new OutputRow(out, chance, count));
		}
	}

	/**
	 * Editing any recipe — built-in or player-created — needs cheat/op
	 * permission; players without it can only change the difficulty.
	 */
	@Override
	protected boolean canEdit() {
		return super.canEdit() && canCheat();
	}

	@Override
	protected int rowsTop() {
		return 40;
	}

	@Override
	protected int rowsBottom() {
		return this.height - 30;
	}

	@Override
	protected void buildExtras() {
		boolean locked = !canEdit();

		// Reset-all: anchored top-right; its left edge is the alignment anchor for
		// every row's Reset button and its right edge for every row's red X.
		Button resetAll = Button.builder(
				Component.translatable("createsifter.config.reset_all"),
				b -> {
					for (OutputRow row : rows) {
						if (row.output == null) continue;
						row.chancePercent = Math.round(row.output.getChance() * 100);
						row.count = row.output.getStack().getCount();
					}
				}).bounds(this.width - 138, 10, 124, 20).build();
		resetAll.active = !locked;
		addRenderableWidget(resetAll);

		// Done: bottom-left, commits staged edits.
		Button done = Button.builder(
				Component.translatable("createsifter.config.done"),
				b -> this.onClose()).bounds(blockX(), this.height - 26, 80, 18).build();
		done.active = !locked;
		addRenderableWidget(done);

		// Cancel: returns without writing anything (unlike Done/Esc, which commit).
		Button cancel = Button.builder(
				Component.translatable("createsifter.config.cancel"),
				b -> this.minecraft.setScreen(parent)).bounds(blockX() + 86, this.height - 26, 60, 18).build();
		cancel.active = !locked;
		addRenderableWidget(cancel);

		// Delete this recipe: red text, same width and alignment as "reset all",
		// sitting on the same row as Done/Cancel.
		Button delete = new ColoredButton(this.width - 138, this.height - 26, 124, 18,
				Component.translatable("createsifter.config.delete_recipe"),
				b -> confirmDelete(), 0xFF5555);
		delete.active = !locked;
		addRenderableWidget(delete);
	}

	private void confirmDelete() {
		if (!canEdit()) return;
		this.minecraft.setScreen(new ConfirmScreen(
				ok -> {
					if (ok) {
						ClientConfigWrites.execute(() -> {
							String id = recipe.getId().toString();
							SifterConfig.setDeleted(id, true);
							SifterConfig.removeCustomRecipe(id);
							SifterConfig.clearOverrides(id);
						});
						this.minecraft.setScreen(parent);
					} else {
						this.minecraft.setScreen(this);
					}
				},
				Component.translatable("createsifter.config.delete_recipe_confirm"),
				Component.translatable("createsifter.config.delete_recipe_warn"),
				CommonComponents.GUI_YES, CommonComponents.GUI_NO));
	}

	/**
	 * Write the edited recipe as a full JSON replacement (plus clearing stale
	 * per-index overrides). Invalid pending rows are dropped here, so a product
	 * whose item never resolved simply disappears.
	 */
	@Override
	protected void commit() {
		if (!canEdit()) return;
		validRows();
		ClientConfigWrites.execute(() -> {
			String id = recipe.getId().toString();
			if (rows.isEmpty()) {
				// All outputs were deleted or never resolved. A player-created
				// recipe is dropped entirely; a built-in recipe reverts to its
				// defaults instead of being replaced by an empty one (which the
				// cleanup on the parent screen would otherwise see as invalid and
				// resurrect the untouched original).
				if (id.startsWith(CustomRecipeStore.CUSTOM_PREFIX))
					SifterConfig.removeCustomRecipe(id);
				SifterConfig.clearOverrides(id);
				return;
			}
			JsonObject json = CustomRecipeStore.toJson(recipe);
			JsonArray results = new JsonArray();
			for (OutputRow r : rows)
				// Serialise the live UI values, not the recipe's original outputs:
				// chance/count are edited in the row and must be written back.
				results.add(makeProcessingOutput(r.output.getStack(), r.chancePercent, r.count).serialize());
			json.add("results", results);
			SifterConfig.putCustomRecipe(id, json);
			SifterConfig.clearOverrides(id);
		});
	}

	@Override
	protected void renderTitle(GuiGraphics graphics) {
		ItemStack siftable = recipe.getSiftableItemStack();
		boolean hasSiftable = !siftable.isEmpty();
		String recipeOf = hasSiftable
				? Component.translatable("createsifter.config.recipe_of", siftable.getHoverName().getString()).getString()
				: recipe.getId().getPath();
		String titleStr = Component.translatable("createsifter.config.recipe_title")
				.append(Component.literal(": " + recipeOf)).getString();
		int gap = 3;
		int iconW = hasSiftable ? gap + 16 : 0;
		int labelW = this.font.width(titleStr);
		int titleX = Math.max(4, this.width / 2 - (labelW + iconW) / 2);
		// Keep clear of the "reset all" button in the top-right corner, and of the
		// item icon after the label, so a long recipe name never slides underneath.
		int maxW = (this.width - 138 - 8) - titleX - iconW - 4;
		if (labelW > maxW && maxW > 8)
			titleStr = this.font.plainSubstrByWidth(titleStr, maxW - this.font.width("…")) + "…";
		graphics.drawString(this.font, titleStr, titleX, 8, 0xFFFFFF);
		if (hasSiftable)
			graphics.renderItem(siftable, titleX + this.font.width(titleStr) + gap, 5);
	}

	@Override
	public void onClose() {
		// Esc and X close the same way as Done: staged edits are committed.
		// Only the Cancel button (which calls setScreen directly) discards them.
		commit();
		this.minecraft.setScreen(parent);
	}
}
