/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.modmenu;

import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared per-recipe editor mechanics: the output row list with chance/count
 * steppers, per-row reset, per-row delete (red X), a pending-item input row,
 * the "add new product" button and list scrolling.
 *
 * Concrete screens (RecipeEditScreen for existing recipes, NewRecipeScreen for
 * brand-new ones) supply the surrounding layout and decide how a commit is
 * written back to the config.
 *
 * 配方编辑的共用机制：产物行的概率/数量步进器、单行重置、单行删除（红叉）、
 * 待输入物品的空产物行、"添加新的产物"按钮与列表滚动。具体界面只负责外围布局
 * 与提交方式。
 */
public abstract class SifterRecipeEditorScreen extends Screen {

	protected final Screen parent;
	protected final List<OutputRow> rows = new ArrayList<>();
	protected final List<Button> rowButtons = new ArrayList<>();
	protected final List<EditBox> rowInputs = new ArrayList<>();
	protected Button addProductButton;
	protected int scroll;
	protected boolean serverConfigLocked;

	protected static final int ROW_H = 24;

	/**
	 * Right-anchored control block: the reset column starts at width-138
	 * (aligned with the "reset all" button above) and the red X ends at
	 * width-14 (aligned with the scrollbar's left edge, clear of it).
	 */
	private static final int RESET_X = 138;
	private static final int RESET_W = 106;
	private static final int RED_X_W = 16;
	private static final int RED_X_X = 30;
	private static final int RED_X_RIGHT = 14;

	/** One row per output; output == null means the player still has to type an item id/name. */
	protected static final class OutputRow {
		ProcessingOutput output;      // null = pending item input
		int chancePercent;            // 0-100
		int count;                    // 1-64
		String pendingText = "";
		EditBox input;                // live widget of a pending row (rebuilt on scroll)
		boolean inputError;           // last Enter-commit attempt failed to resolve an item

		OutputRow(ProcessingOutput output, int chancePercent, int count) {
			this.output = output;
			this.chancePercent = chancePercent;
			this.count = count;
		}

		OutputRow() {
			// Default 5% chance for a just-added product; the player can adjust it
			// with the row steppers after confirming the item.
			this(null, 5, 1);
		}
	}

	protected SifterRecipeEditorScreen(Component title, Screen parent) {
		super(title);
		this.parent = parent;
	}

	/** Left edge of the centered edit block. */
	protected int blockX() {
		return Math.max(14, (this.width - 460) / 2);
	}

	/** First y of the output list area. */
	protected abstract int rowsTop();

	/** Bottom y of the output list area (the add-product button sits just above it). */
	protected abstract int rowsBottom();

	/** Whether config writes are possible right now (single-player local server, not locked). */
	protected boolean canEdit() {
		return !serverConfigLocked && !SifterConfig.isRecipeEditLocked();
	}

	/**
	 * Whether the local player may edit recipes and pick the ULTRA preset.
	 * Single-player/LAN-host: cheats are granted when the world itself allows
	 * them, or when the world was opened to LAN with "Allow Cheats" on — the
	 * classic outlet for a non-cheat save. Closing the LAN session closes the
	 * outlet again. On a remote connection the whole screen is already read-only
	 * via serverConfigLocked, so the op-2 fallback below is only a defensive
	 * last line (it never enables writes by itself).
	 */
	protected static boolean canCheat() {
		Minecraft mc = Minecraft.getInstance();
		if (mc == null || mc.player == null) return false;
		if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
			MinecraftServer server = mc.getSingleplayerServer();
			return server.getWorldData().getAllowCommands()
					|| (server.isPublished() && server.getPlayerList().isAllowCheatsForAllPlayers());
		}
		return mc.player.hasPermissions(2);
	}

	/** Called by Done / Esc; subclasses write the staged rows back. */
	protected abstract void commit();

	/** Title block drawn above the columns. */
	protected abstract void renderTitle(GuiGraphics graphics);

	/** Extra widgets (header/footer buttons) created by the subclass. */
	protected void buildExtras() {}

	/** Whether pending item rows must resolve to a block (new-recipe siftable only). */
	protected boolean requireBlockInput() {
		return false;
	}

	@Override
	protected void init() {
		this.clearWidgets();
		Minecraft mc = Minecraft.getInstance();
		this.serverConfigLocked = mc.level == null || (mc.getCurrentServer() != null && !mc.hasSingleplayerServer());
		buildExtras();
		addProductButton = Button.builder(
				Component.translatable("createsifter.config.add_product"),
				b -> addNewProduct()).bounds(blockX(), rowsBottom() - 22, 100, 18).build();
		addProductButton.active = canEdit();
		addRenderableWidget(addProductButton);
		rebuildRows();
	}

	// --- Row editing actions ---

	protected void addNewProduct() {
		if (!canEdit()) return;
		rows.add(new OutputRow());
		scroll = Math.max(0, rows.size() - 1);
		rebuildRows();
	}

	protected void adjust(int outputIndex, int field, int delta) {
		OutputRow row = rows.get(outputIndex);
		if (row.output == null) return;
		if (field == 0) {
			row.chancePercent = Math.max(0, Math.min(100, row.chancePercent + delta));
		} else {
			// Cap at the item's stack size so the shown count always matches what
			// gets written (makeProcessingOutput clamps again as a safety net).
			int max = Math.min(SifterConfig.MAX_OVERRIDE_COUNT, row.output.getStack().getMaxStackSize());
			row.count = Math.max(1, Math.min(max, row.count + delta));
		}
	}

	protected void removeRow(int outputIndex) {
		rows.remove(outputIndex);
		if (scroll > 0 && scroll >= rows.size())
			scroll = Math.max(0, rows.size() - 1);
		rebuildRows();
	}

	/** Resolve a pending row's typed text into a real output. */
	protected boolean tryCommitInput(OutputRow row) {
		ItemStack stack = ItemParseHelper.parse(row.pendingText, requireBlockInput());
		if (stack.isEmpty()) {
			row.inputError = true;
			return false;
		}
		row.output = makeProcessingOutput(stack, row.chancePercent, row.count);
		row.inputError = false;
		row.pendingText = "";
		rebuildRows();
		return true;
	}

	/**
	 * Resolve every pending row and drop the ones that never resolved. A typed
	 * but unconfirmed product (Enter was not pressed) is parsed here, so saving
	 * keeps it instead of silently discarding it; only truly invalid text is
	 * dropped.
	 */
	protected List<OutputRow> validRows() {
		for (OutputRow r : rows) {
			if (r.output == null && !r.pendingText.isEmpty()) {
				ItemStack stack = ItemParseHelper.parse(r.pendingText, requireBlockInput());
				if (!stack.isEmpty())
					r.output = makeProcessingOutput(stack, r.chancePercent, r.count);
			}
		}
		rows.removeIf(r -> r.output == null || r.output.getStack().isEmpty());
		return rows;
	}

	protected static ProcessingOutput makeProcessingOutput(ItemStack stack, int chancePercent, int count) {
		JsonObject j = new JsonObject();
		j.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
		j.addProperty("chance", Math.max(0f, Math.min(1f, chancePercent / 100f)));
		// The JSON count is used directly at roll time (no override clamp anymore),
		// so cap it at the item's stack size to avoid oversized drops.
		j.addProperty("count", Math.max(1, Math.min(count, stack.getMaxStackSize())));
		return ProcessingOutput.deserialize(j);
	}

	// --- Widget rebuild ---

	private void clearRows() {
		for (Button b : rowButtons) removeWidget(b);
		for (EditBox e : rowInputs) removeWidget(e);
		rowButtons.clear();
		rowInputs.clear();
	}

	protected void rebuildRows() {
		clearRows();
		int bx = blockX();
		boolean locked = !canEdit();
		int outputCount = rows.size();
		int top = rowsTop();
		int bottom = rowsBottom() - 24; // leave room for the add-product button
		int maxRows = Math.max(1, (bottom - top) / ROW_H);
		int first = Math.min(scroll, Math.max(0, outputCount - maxRows));
		this.scroll = first;
		for (int row = 0; row < maxRows; row++) {
			int idx = first + row;
			if (idx >= outputCount) break;
			final int idxi = idx;
			int y = top + row * ROW_H;
			OutputRow out = rows.get(idx);
			if (out.output == null) {
				// Pending row: item input box + red X; chance/count stay at 0% / 1x.
				int boxW = Math.max(60, (this.width - RED_X_X) - bx - 8);
				EditBox box = new EditBox(this.font, bx, y + 1, boxW, 16, Component.literal(""));
				box.setMaxLength(80);
				box.setHint(Component.translatable("createsifter.config.enter_item"));
				box.setValue(out.pendingText);
				box.setResponder(t -> {
					out.pendingText = t;
					out.inputError = false; // typing again clears the "invalid" flag
				});
				box.setEditable(!locked);
				out.input = box;
				rowInputs.add(box);
				rowButtons.add(makeDeleteButton(idxi, y));
			} else {
				rowButtons.add(Button.builder(Component.literal("<"), b -> adjust(idxi, 0, -5)).bounds(bx, y, 20, 18).build());
				rowButtons.add(Button.builder(Component.literal(">"), b -> adjust(idxi, 0, +5)).bounds(bx + 22, y, 20, 18).build());
				rowButtons.add(Button.builder(Component.literal("-"), b -> adjust(idxi, 1, -1)).bounds(bx + 66, y, 20, 18).build());
				rowButtons.add(Button.builder(Component.literal("+"), b -> adjust(idxi, 1, +1)).bounds(bx + 88, y, 20, 18).build());
				rowButtons.add(Button.builder(Component.translatable("createsifter.config.reset"),
						b -> {
							OutputRow r = rows.get(idxi);
							r.chancePercent = Math.round(r.output.getChance() * 100);
							r.count = r.output.getStack().getCount();
						}).bounds(this.width - RESET_X, y, RESET_W, 18).build());
				rowButtons.add(makeDeleteButton(idxi, y));
			}
		}
		for (Button b : rowButtons) b.active = !locked;
		for (Button b : rowButtons) addRenderableWidget(b);
		for (EditBox e : rowInputs) addRenderableWidget(e);
	}

	/** Red X button aligned so its right edge matches the "reset all" right edge. */
	private Button makeDeleteButton(int idx, int y) {
		return new ColoredButton(this.width - RED_X_X, y, RED_X_W, 18, Component.literal("×"),
				b -> removeRow(idx), 0xFF5555);
	}

	/**
	 * Button with an explicit foreground colour. Minecraft 1.20.1's Button has a
	 * protected constructor and no foreground-colour setter, so subclasses are
	 * the only way to build one and tint its label (red X / red "delete recipe").
	 */
	protected static class ColoredButton extends Button {
		private final int color;

		ColoredButton(int x, int y, int width, int height, Component message, OnPress onPress, int color) {
			super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
			this.color = color;
		}

		@Override
		public void renderString(GuiGraphics guiGraphics, Font font, int i) {
			int j = this.color | Mth.ceil(this.alpha * 255.0F) << 24;
			guiGraphics.drawCenteredString(font, this.getMessage(),
					this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 8) / 2, j);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		int outputCount = rows.size();
		int maxRows = Math.max(1, (rowsBottom() - 24 - rowsTop()) / ROW_H);
		int maxScroll = Math.max(0, outputCount - maxRows);
		int before = this.scroll;
		this.scroll = (int) Math.max(0, Math.min(maxScroll, this.scroll - delta));
		if (this.scroll != before) rebuildRows();
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			for (EditBox e : rowInputs) {
				if (e.isFocused()) {
					for (OutputRow r : rows) {
						if (r.output == null && r.input == e) {
							tryCommitInput(r);
							return true;
						}
					}
				}
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	// --- Rendering ---

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
		renderTitle(graphics);
		renderColumns(graphics);
		renderRows(graphics);

		if (serverConfigLocked)
			graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.readonly_multiplayer"),
					this.width / 2, rowsBottom() - 6, 0xFF5555);
		else
			graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.saved_on_close"),
					this.width / 2, rowsBottom() - 6, 0x888888);
		super.render(graphics, mouseX, mouseY, partialTick);
	}

	protected void renderColumns(GuiGraphics graphics) {
		int bx = blockX();
		int colY = rowsTop() - 18;
		graphics.drawString(this.font, Component.translatable("createsifter.config.chance_col"), bx + 44, colY, 0xFFFF55);
		graphics.drawString(this.font, Component.translatable("createsifter.config.count_col"), bx + 112, colY, 0xFFFF55);
		graphics.drawString(this.font, Component.translatable("createsifter.config.output_col"), bx + 150, colY, 0xFFFF55);
	}

	protected void renderRows(GuiGraphics graphics) {
		int bx = blockX();
		int top = rowsTop();
		int bottom = rowsBottom() - 24;
		int maxRows = Math.max(1, (bottom - top) / ROW_H);
		graphics.enableScissor(bx, top, this.width - bx, bottom);
		for (int row = 0; row < maxRows; row++) {
			int idx = this.scroll + row;
			if (idx >= rows.size()) break;
			OutputRow out = rows.get(idx);
			int y = top + row * ROW_H;
			if ((row & 1) == 0)
				graphics.fill(bx, y, this.width - bx, y + ROW_H - 2, 0x11000000);
			if (out.output == null) {
				// Pending row: the default chance/count sits below the input box (the
				// box itself spans the whole row width, so nothing can overlap it).
				graphics.drawString(this.font, out.chancePercent + "% x" + out.count, bx + 8, y + 18, 0x888888);
				// "Invalid" only after an Enter attempt failed, never while typing.
				if (out.inputError)
					graphics.drawString(this.font, Component.translatable("createsifter.config.invalid_item"),
							bx + 44, y + 18, 0xFF5555);
			} else {
				graphics.drawString(this.font, out.chancePercent + "%", bx + 44, y + 3, 0x55FFFF);
				graphics.drawString(this.font, "x" + out.count, bx + 112, y + 3, 0x55FF55);
				graphics.renderItem(out.output.getStack(), bx + 150, y);
				String name = out.output.getStack().getHoverName().getString();
				int nameX = bx + 172;
				// Cap at the per-row Reset button's left edge so a long name never
				// slides underneath it (the red X column beyond it is already clear).
				int maxW = (this.width - RESET_X) - nameX - 4;
				if (this.font.width(name) > maxW && maxW > 8)
					name = this.font.plainSubstrByWidth(name, maxW - this.font.width("…")) + "…";
				graphics.drawString(this.font, name, nameX, y + 3, 0xFFFFFF);
			}
		}
		graphics.disableScissor();
		// Scrollbar
		if (rows.size() > maxRows) {
			int barH = Math.max(10, (bottom - top) * maxRows / rows.size());
			int barY = top + (int) ((bottom - top - barH) * (double) this.scroll / Math.max(1, rows.size() - maxRows));
			graphics.fill(this.width - 14, barY, this.width - 12, barY + barH, 0xFFAAAAAA);
		}
	}
}
