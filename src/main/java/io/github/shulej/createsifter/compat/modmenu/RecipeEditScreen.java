/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.modmenu;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-recipe editor. For each output of a sifting recipe the player can change
 * the drop chance (0-100%) and the amount per success. Changes are staged in
 * memory and written to the server-side override table when the screen closes
 * (single-player only; on a remote server the screen is read-only).
 *
 * 单条配方的编辑界面：可逐条修改每个产物的掉落概率（0-100%）与每次数量。
 * 改动先暂存在内存中，关闭界面时统一提交到服务端配置表中并保存。
 */
public class RecipeEditScreen extends Screen {

	private final Screen parent;
	private final SiftingRecipe recipe;
	private final List<OutputRow> rows = new ArrayList<>();
	private final List<Button> rowButtons = new ArrayList<>();
	private int scroll;

	private static final int ROW_H = 24;

	/** One row per output; overrides are staged here and only committed on close. */
	private static final class OutputRow {
		final ProcessingOutput output;
		int chancePercent;
		int count;
		OutputRow(ProcessingOutput output, int chancePercent, int count) {
			this.output = output;
			this.chancePercent = chancePercent;
			this.count = count;
		}
	}

	public RecipeEditScreen(Screen parent, SiftingRecipe recipe) {
		super(Component.translatable("createsifter.config.recipe_title"));
		this.parent = parent;
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

	/** Left edge of the centered edit block; wide enough to hold steppers/value/icon. */
	private int blockX() {
		return Math.max(14, (this.width - 460) / 2);
	}

	private boolean serverConfigLocked() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level == null || (mc.getCurrentServer() != null && !mc.hasSingleplayerServer());
	}

	@Override
	protected void init() {
		this.clearWidgets();
		int bx = blockX();
		boolean locked = serverConfigLocked();

		// Reset-all: anchored to the top-right so it never overlaps the centered block.
		Button resetAll = Button.builder(
				Component.translatable("createsifter.config.reset_all"),
				b -> {
					for (OutputRow row : rows) {
						row.chancePercent = Math.round(row.output.getChance() * 100);
						row.count = row.output.getStack().getCount();
					}
				}).bounds(this.width - 138, 10, 128, 20).build();
		resetAll.active = !locked;
		addRenderableWidget(resetAll);

		// Done: bottom-left, commits staged edits. Disabled while read-only, where
		// nothing can be written and Cancel is the only sensible exit.
		Button done = Button.builder(
				Component.translatable("createsifter.config.done"),
				b -> this.onClose()).bounds(bx, this.height - 26, 80, 18).build();
		done.active = !locked;
		addRenderableWidget(done);

		// Cancel: returns without writing anything (unlike Done/Esc, which commit).
		addRenderableWidget(Button.builder(
				Component.translatable("createsifter.config.cancel"),
				b -> this.minecraft.setScreen(parent)).bounds(bx + 86, this.height - 26, 60, 18).build());

		rebuildRows();
	}

	private void clearRows() {
		for (Button b : rowButtons) removeWidget(b);
		rowButtons.clear();
	}

	private void rebuildRows() {
		clearRows();
		int bx = blockX();
		boolean locked = serverConfigLocked();
		int outputCount = rows.size();
		int top = 40;
		int maxRows = Math.max(1, (this.height - top - 30) / ROW_H);
		int first = Math.min(scroll, Math.max(0, outputCount - maxRows));
		this.scroll = first;
		for (int row = 0; row < maxRows; row++) {
			int idx = first + row;
			if (idx >= outputCount) break;
			final int idxi = idx;
			int y = top + row * ROW_H;
			// Chance steppers
			rowButtons.add(Button.builder(Component.literal("<"), b -> adjust(idxi, 0, -5)).bounds(bx, y, 20, 18).build());
			rowButtons.add(Button.builder(Component.literal(">"), b -> adjust(idxi, 0, +5)).bounds(bx + 22, y, 20, 18).build());
			// Count steppers
			rowButtons.add(Button.builder(Component.literal("-"), b -> adjust(idxi, 1, -1)).bounds(bx + 66, y, 20, 18).build());
			rowButtons.add(Button.builder(Component.literal("+"), b -> adjust(idxi, 1, +1)).bounds(bx + 88, y, 20, 18).build());
			// Reset single output (right-aligned, clear of the scrollbar)
			rowButtons.add(Button.builder(Component.translatable("createsifter.config.reset"),
					b -> {
						OutputRow r = rows.get(idxi);
						r.chancePercent = Math.round(r.output.getChance() * 100);
						r.count = r.output.getStack().getCount();
					}).bounds(this.width - 132, y, 106, 18).build());
			for (Button b : rowButtons.subList(rowButtons.size() - 5, rowButtons.size()))
				b.active = !locked;
		}
		for (Button b : rowButtons) addRenderableWidget(b);
	}

	private void adjust(int outputIndex, int field, int delta) {
		OutputRow row = rows.get(outputIndex);
		if (field == 0) {
			row.chancePercent = Math.max(0, Math.min(100, row.chancePercent + delta));
		} else {
			row.count = Math.max(1, Math.min(SifterConfig.MAX_OVERRIDE_COUNT, row.count + delta));
		}
	}

	/** Write every staged row into the config and persist, on the server's thread. */
	private void commit() {
		if (serverConfigLocked()) return;
		ClientConfigWrites.execute(() -> {
			for (int i = 0; i < rows.size(); i++) {
				OutputRow row = rows.get(i);
				SifterConfig.setOverride(recipe.getId().toString(), i, row.chancePercent, row.count);
			}
		});
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		int outputCount = rows.size();
		int maxRows = Math.max(1, (this.height - 40 - 30) / ROW_H);
		int maxScroll = Math.max(0, outputCount - maxRows);
		int before = this.scroll;
		this.scroll = (int) Math.max(0, Math.min(maxScroll, this.scroll - delta));
		if (this.scroll != before) rebuildRows();
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.recipe_title")
				.append(" : " + recipe.getId().getPath()), this.width / 2, 8, 0xFFFFFF);

		int bx = blockX();
		int outputCount = rows.size();
		int top = 40;
		int maxRows = Math.max(1, (this.height - top - 30) / ROW_H);
		int listBottom = this.height - 30;

		// Column headings aligned with the value columns below.
		graphics.drawString(this.font, Component.translatable("createsifter.config.chance_col"), bx + 44, 22, 0xFFFF55);
		graphics.drawString(this.font, Component.translatable("createsifter.config.count_col"), bx + 112, 22, 0xFFFF55);
		graphics.drawString(this.font, Component.translatable("createsifter.config.output_col"), bx + 150, 22, 0xFFFF55);

		graphics.enableScissor(bx, top, this.width - bx, listBottom);
		for (int row = 0; row < maxRows; row++) {
			int idx = this.scroll + row;
			if (idx >= outputCount) break;
			OutputRow out = rows.get(idx);
			int y = top + row * ROW_H;
			if ((row & 1) == 0)
				graphics.fill(bx, y, this.width - bx, y + ROW_H - 2, 0x11000000);
			graphics.drawString(this.font, out.chancePercent + "%", bx + 44, y + 3, 0x55FFFF);
			graphics.drawString(this.font, "x" + out.count, bx + 112, y + 3, 0x55FF55);
			// Output icon + name.
			graphics.renderItem(out.output.getStack(), bx + 150, y);
			String name = out.output.getStack().getHoverName().getString();
			int nameX = bx + 172;
			int maxW = (this.width - 132) - nameX;
			if (this.font.width(name) > maxW)
				name = this.font.plainSubstrByWidth(name, maxW - this.font.width("…")) + "…";
			graphics.drawString(this.font, name, nameX, y + 3, 0xFFFFFF);
		}
		graphics.disableScissor();
		// Scrollbar
		if (outputCount > maxRows) {
			int barH = Math.max(10, (listBottom - top) * maxRows / outputCount);
			int barY = top + (int) ((listBottom - top - barH) * (double) this.scroll / Math.max(1, outputCount - maxRows));
			graphics.fill(this.width - 14, barY, this.width - 12, barY + barH, 0xFFAAAAAA);
		}

		if (serverConfigLocked())
			graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.readonly_multiplayer"), this.width / 2, this.height - 14, 0xFF5555);
		else
			graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.saved_on_close"), this.width / 2, this.height - 14, 0x888888);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void onClose() {
		// Esc and X close the same way as Done: staged edits are committed.
		// Only the Cancel button (which calls setScreen directly) discards them.
		commit();
		this.minecraft.setScreen(parent);
	}
}
