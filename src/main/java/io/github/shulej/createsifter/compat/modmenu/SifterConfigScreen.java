/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.modmenu;

import io.github.shulej.createsifter.ModRecipeTypes;
import io.github.shulej.createsifter.content.contraptions.components.sifter.Difficulty;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import io.github.shulej.createsifter.foundation.data.recipe.CustomRecipeStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Main config screen launched from the Mod Menu "Config" button.
 *
 * Two layers controlled here:
 *  1) A global difficulty preset (ULTRA / HIGH / MEDIUM / LOW / CUSTOM). The
 *     CUSTOM mode exposes two +/- steppers for output chance and processing time.
 *  2) A scrollable per-recipe editor: each sifting recipe's drop chance (%) and
 *     per-success amount can be overridden; saved into the server-side override
 *     table and applied at roll time.
 *
 * On a dedicated (or LAN-remote) server these edits cannot reach the server and
 * the whole screen is read-only.
 * 主配置界面：全局难度预设 + 可滚动的逐配方概率/数量编辑，双语，服务端配置为准。
 */
public class SifterConfigScreen extends Screen {

	private final Screen parent;
	private Difficulty selected;
	private int scroll;
	private final List<Button> editButtons = new ArrayList<>();
	private final List<Button> headerButtons = new ArrayList<>();
	private final List<SiftingRecipe> recipes = new ArrayList<>();
	private boolean serverConfigLocked;
	private boolean snapshotTaken;
	private boolean permissionWarned;
	private Button doneButton;

	private static final int LIST_X = 12;
	private static final int LIST_Y_TOP = 78;
	private static final int ROW_HEIGHT = 22;

	// Per-frame work in render() is limited to what must change each frame:
	// the per-recipe summaries are cached and rebuilt only when the override
	// table changes (single-player edits go through RecipeEditScreen).
	private int summaryGen = -1;
	private final Map<SiftingRecipe, String> summaryCache = new HashMap<>();
	/** Last recipe-table generation the list was rebuilt from (refreshes after sub-screen writes). */
	private int recipeListGen = -1;

	public SifterConfigScreen(Screen parent) {
		super(Component.translatable("createsifter.config.title"));
		this.parent = parent;
		this.selected = Difficulty.current();
	}

	@Override
	protected void init() {
		this.clearWidgets();
		Minecraft mc = Minecraft.getInstance();
		// Edits only work while a local server runs in this JVM: the port attaches
		// the server config file on SERVER_STARTING, so at the title screen (no
		// level) and on remote servers there is no backing config to write to.
		this.serverConfigLocked = mc.level == null || (mc.getCurrentServer() != null && !mc.hasSingleplayerServer());

		// Snapshot once per screen session (returning from a sub-screen re-runs
		// init but must not refresh the snapshot, or Cancel could not roll back
		// edits made before the sub-screen was opened).
		if (!snapshotTaken) {
			SifterConfig.snapshotRecipeEdits();
			snapshotTaken = true;
		}

		// First-open notice: without cheat/op permission most of this screen is
		// read-only. Show it as a dialog once per session instead of an inline
		// label that would get truncated.
		if (!permissionWarned && !serverConfigLocked && !SifterConfig.isRecipeEditLocked() && !canCheat()) {
			permissionWarned = true;
			this.minecraft.setScreen(new ConfirmScreen(
					ok -> this.minecraft.setScreen(this),
					Component.translatable("createsifter.config.permission_title"),
					Component.translatable("createsifter.config.requires_cheats"),
					CommonComponents.GUI_DONE, CommonComponents.GUI_CANCEL));
			return;
		}

		collectRecipes();
		buildHeader();
		rebuildRecipeList();
	}

	private void buildHeader() {
		for (Button b : headerButtons) removeWidget(b);
		headerButtons.clear();

		boolean editLocked = SifterConfig.isRecipeEditLocked();

		// --- Difficulty preset row (5 buttons) ---
		Difficulty[] presets = new Difficulty[]{Difficulty.ULTRA, Difficulty.HIGH, Difficulty.MEDIUM, Difficulty.LOW, Difficulty.CUSTOM};
		int presetGap = 4;
		int presetW = Math.min(66, Math.max(40, (this.width - 2 * LIST_X - 4 * presetGap) / presets.length));
		int presetTotal = presets.length * presetW + (presets.length - 1) * presetGap;
		int bx = this.width / 2 - presetTotal / 2;
		int i = 0;
		for (Difficulty d : presets) {
			final Difficulty dd = d;
			Button b = Button.builder(presetLabel(d), b2 -> choose(dd)).bounds(bx + i * (presetW + presetGap), 34, presetW, 20).build();
			// ULTRA needs cheat/op permission; the whole row is off while the
			// recipe edits are locked.
			b.active = !serverConfigLocked && !editLocked && (dd != Difficulty.ULTRA || canCheat());
			headerButtons.add(b);
			addRenderableWidget(b);
			i++;
		}

		// --- CUSTOM steppers (chance steps by 5 percent, time by 0.1) ---
		if (selected == Difficulty.CUSTOM && !serverConfigLocked) {
			int cw = this.width / 2;
			// Steppers stay clear of the value labels drawn in render(): chance is
			// anchored at (cw-168, 58) with the steppers right after it, time at
			// (cw+94, 58) with the steppers right before it. Label widths vary by
			// locale and value, so the stepper x is derived from the measured text.
			int chanceStepperX = cw - 168 + this.font.width(chanceLabel()) + 6;
			Button chanceMinus = Button.builder(Component.literal("<"), b -> stepChance(-5)).bounds(chanceStepperX, 56, 18, 18).build();
			Button chancePlus = Button.builder(Component.literal(">"), b -> stepChance(+5)).bounds(chanceStepperX + 18, 56, 18, 18).build();
			int timeStepperX = cw + 94 - this.font.width(timeLabel()) - 24;
			Button timeMinus = Button.builder(Component.literal("<"), b -> stepTime(-0.1)).bounds(timeStepperX, 56, 18, 18).build();
			Button timePlus = Button.builder(Component.literal(">"), b -> stepTime(+0.1)).bounds(timeStepperX + 18, 56, 18, 18).build();
			chanceMinus.active = !editLocked;
			chancePlus.active = !editLocked;
			timeMinus.active = !editLocked;
			timePlus.active = !editLocked;
			headerButtons.add(chanceMinus);
			headerButtons.add(chancePlus);
			headerButtons.add(timeMinus);
			headerButtons.add(timePlus);
			for (Button b : headerButtons.subList(headerButtons.size() - 4, headerButtons.size()))
				addRenderableWidget(b);
		}

		// --- Done button (fixed at bottom, centre) ---
		doneButton = Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
				.bounds(this.width / 2 - 80, this.height - 28, 160, 20).build();
		headerButtons.add(doneButton);
		addRenderableWidget(doneButton);

		// --- Cancel: restores the session snapshot and leaves without saving ---
		Button cancel = Button.builder(
				Component.translatable("createsifter.config.cancel"),
				b -> cancelAndExit()).bounds(this.width / 2 + 86, this.height - 28, 60, 20).build();
		cancel.active = !serverConfigLocked;
		headerButtons.add(cancel);
		addRenderableWidget(cancel);

		// --- Add new recipe (fixed just above the list bottom); needs cheat/op. ---
		Button addNew = Button.builder(
				Component.translatable("createsifter.config.add_recipe"),
				b -> openNewRecipe()).bounds(LIST_X, this.height - 62, 120, 20).build();
		addNew.active = !serverConfigLocked && !editLocked && canCheat();
		headerButtons.add(addNew);
		addRenderableWidget(addNew);

		// --- Reset all recipes (top-right, keeps the title row clear) ---
		Button resetAll = Button.builder(
				Component.translatable("createsifter.config.reset_all_recipes"),
				b -> confirmResetAll()).bounds(this.width - 138, 10, 124, 20).build();
		resetAll.active = !serverConfigLocked && !editLocked && canCheat();
		headerButtons.add(resetAll);
		addRenderableWidget(resetAll);

		// --- Recipe-edit lock (bottom-right, same column as reset-all above).
		// Locking is open to any player (cheats on or off) and is permanent:
		// once locked there is no in-game way back, so the button turns into a
		// disabled "locked" label with no unlock path. ---
		Button lockButton;
		if (editLocked) {
			lockButton = Button.builder(
					Component.translatable("createsifter.config.edits_locked"),
					b -> {}).bounds(this.width - 138, this.height - 62, 124, 20).build();
			lockButton.active = false;
		} else {
			lockButton = Button.builder(
					Component.translatable("createsifter.config.lock_edits"),
					b -> confirmLock()).bounds(this.width - 138, this.height - 62, 124, 20).build();
			lockButton.active = !serverConfigLocked;
		}
		headerButtons.add(lockButton);
		addRenderableWidget(lockButton);
	}

	/**
	 * Two-step confirmation before locking: the first dialog only warns, the
	 * second spells out the consequence (the lock cannot be undone in-game).
	 */
	private void confirmLock() {
		if (serverConfigLocked || SifterConfig.isRecipeEditLocked()) return;
		this.minecraft.setScreen(new ConfirmScreen(
				first -> {
					if (!first) {
						this.minecraft.setScreen(this);
						return;
					}
					this.minecraft.setScreen(new ConfirmScreen(
							second -> {
								if (second)
									ClientConfigWrites.execute(() -> SifterConfig.setRecipeEditLocked(true));
								this.minecraft.setScreen(this);
							},
							Component.translatable("createsifter.config.lock_edits_confirm2"),
							Component.translatable("createsifter.config.lock_edits_warn2"),
							CommonComponents.GUI_YES, CommonComponents.GUI_NO));
				},
				Component.translatable("createsifter.config.lock_edits_confirm1"),
				Component.translatable("createsifter.config.lock_edits_warn1"),
				CommonComponents.GUI_YES, CommonComponents.GUI_NO));
	}

	private void openNewRecipe() {
		if (serverConfigLocked || SifterConfig.isRecipeEditLocked() || !canCheat()) return;
		String id = CustomRecipeStore.nextCustomId();
		this.minecraft.setScreen(new NewRecipeScreen(this, id));
	}

	private void confirmResetAll() {
		if (serverConfigLocked || SifterConfig.isRecipeEditLocked() || !canCheat()) return;
		this.minecraft.setScreen(new ConfirmScreen(
				ok -> {
					if (ok)
						ClientConfigWrites.execute(SifterConfig::resetAllRecipeEdits);
					this.minecraft.setScreen(this);
				},
				Component.translatable("createsifter.config.reset_all_recipes_confirm"),
				Component.translatable("createsifter.config.reset_all_recipes_warn"),
				CommonComponents.GUI_YES, CommonComponents.GUI_NO));
	}

	private void cancelAndExit() {
		// On a locked session (title screen / remote server) nothing was ever
		// written, and restoring would hit an unloaded config (NPE), so skip.
		if (!serverConfigLocked)
			ClientConfigWrites.execute(SifterConfig::restoreRecipeEdits);
		this.minecraft.setScreen(parent);
	}

	private void collectRecipes() {
		recipes.clear();
		Minecraft mc = this.minecraft;
		if (mc == null || mc.level == null || mc.level.getRecipeManager() == null)
			return;
		try {
			recipes.addAll(ModRecipeTypes.customizedSiftingRecipes(mc.level));
		} catch (Throwable ignored) {
			recipes.clear();
		}
	}

	private int visibleRows() {
		return Math.max(1, (this.height - 62 - LIST_Y_TOP) / ROW_HEIGHT);
	}

	private String recipeSummary(SiftingRecipe r) {
		int gen = SifterConfig.overrideGeneration();
		if (gen != summaryGen) {
			summaryCache.clear();
			summaryGen = gen;
		}
		String cached = summaryCache.get(r);
		if (cached != null)
			return cached;
		StringBuilder sb = new StringBuilder();
		List<com.simibubi.create.content.processing.recipe.ProcessingOutput> outs = r.getRollableResults();
		for (int i = 0; i < outs.size(); i++) {
			com.simibubi.create.content.processing.recipe.ProcessingOutput o = outs.get(i);
			if (i > 0) sb.append(", ");
			int[] ov = SifterConfig.getOverride(r.getId().toString(), i);
			int chance = ov != null ? ov[0] : Math.round(o.getChance() * 100);
			int count = ov != null ? ov[1] : o.getStack().getCount();
			sb.append(o.getStack().getHoverName().getString()).append(" ").append(chance).append("% x").append(count);
		}
		String summary = sb.toString();
		summaryCache.put(r, summary);
		return summary;
	}

	private void rebuildRecipeList() {
		for (Button b : editButtons) removeWidget(b);
		editButtons.clear();

		boolean editLocked = SifterConfig.isRecipeEditLocked();
		int startX = LIST_X;
		int top = LIST_Y_TOP;
		int rows = visibleRows();
		for (int row = 0; row < rows; row++) {
			int idx = row + scroll;
			if (idx >= recipes.size()) break;
			SiftingRecipe recipe = recipes.get(idx);
			int y = top + row * ROW_HEIGHT;
			final SiftingRecipe r = recipe;
			Button edit = Button.builder(Component.literal("✎"), b -> this.minecraft.setScreen(new RecipeEditScreen(this, r)))
					.bounds(startX, y, 18, 18).build();
			// Editing any recipe (built-in or custom) needs cheat/op permission;
			// the whole list is read-only while edits are locked.
			edit.active = !serverConfigLocked && !editLocked && canCheat();
			addRenderableWidget(edit);
			editButtons.add(edit);
		}
	}

	private static boolean canCheat() {
		return SifterRecipeEditorScreen.canCheat();
	}

	private Component presetLabel(Difficulty d) {
		boolean active = selected == d;
		String prefix = active ? "✓ " : "";
		switch (d) {
		case ULTRA: return Component.literal(prefix).append(Component.translatable("createsifter.config.preset.ultra"));
		case HIGH: return Component.literal(prefix).append(Component.translatable("createsifter.config.preset.high"));
		case MEDIUM: return Component.literal(prefix).append(Component.translatable("createsifter.config.preset.medium"));
		case LOW: return Component.literal(prefix).append(Component.translatable("createsifter.config.preset.low"));
		default: return Component.literal(prefix).append(Component.translatable("createsifter.config.preset.custom"));
		}
	}

	private void choose(Difficulty d) {
		if (serverConfigLocked || SifterConfig.isRecipeEditLocked()) return;
		if (d == Difficulty.ULTRA && !canCheat()) return;
		ClientConfigWrites.execute(() -> SifterConfig.DIFFICULTY.set(d));
		this.selected = d;
		// Only the preset row and CUSTOM steppers depend on the selection; the
		// recipe list is unaffected, so rebuild just the header.
		buildHeader();
	}

	private void stepChance(int deltaPercent) {
		if (serverConfigLocked || SifterConfig.isRecipeEditLocked()) return;
		int v = Math.round(SifterConfig.customChanceMultiplier() * 100) + deltaPercent;
		final double clamped = Math.max(5, Math.min(100, v)) / 100.0;
		ClientConfigWrites.execute(() -> SifterConfig.CUSTOM_CHANCE_MULTIPLIER.set(clamped));
	}

	private void stepTime(double delta) {
		if (serverConfigLocked || SifterConfig.isRecipeEditLocked()) return;
		double v = SifterConfig.customTimeMultiplier() + delta;
		final double clamped = Math.max(0.1, Math.min(10.0, Math.round(v * 10) / 10.0));
		ClientConfigWrites.execute(() -> SifterConfig.CUSTOM_TIME_MULTIPLIER.set(clamped));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		int maxScroll = Math.max(0, recipes.size() - visibleRows());
		int before = this.scroll;
		this.scroll = (int) Math.max(0, Math.min(maxScroll, this.scroll - delta));
		if (this.scroll != before) rebuildRecipeList();
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		// Sub-screens write through the server executor, so the config table can
		// change after we returned. Rebuild the list when the generation moves so
		// the new/edited/deleted recipe shows up without needing a screen reopen.
		int gen = SifterConfig.recipeTableGeneration();
		if (gen != recipeListGen) {
			recipeListGen = gen;
			collectRecipes();
			rebuildRecipeList();
			summaryGen = -1;
		}
		this.renderBackground(graphics);
		// Right edge that must stay clear of the "reset all recipes" button
		// (left edge at width-138): keep the title and the description line under it.
		int rightLimit = this.width - 146;

		String title = Component.translatable("createsifter.config.title").getString();
		int titleMax = Math.max(20, rightLimit - 4);
		if (this.font.width(title) > titleMax)
			title = this.font.plainSubstrByWidth(title, titleMax - this.font.width("…")) + "…";
		int titleX = Math.max(4, this.width / 2 - this.font.width(title) / 2);
		if (titleX + this.font.width(title) > rightLimit)
			titleX = Math.max(4, rightLimit - this.font.width(title));
		graphics.drawString(this.font, title, titleX, 12, 0xFFFFFF);

		// Preset description, with the lock reason appended on the same line.
		String note;
		if (serverConfigLocked) {
			note = this.minecraft.level == null
					? Component.translatable("createsifter.config.enter_world").getString()
					: Component.translatable("createsifter.config.readonly_multiplayer").getString();
		} else {
			note = Component.translatable("createsifter.config.server_wins").getString();
		}
		String desc = describe(selected) + "  ·  " + note;
		if (SifterConfig.isRecipeEditLocked())
			desc += "  ·  " + Component.translatable("createsifter.config.edits_locked").getString();
		int descMax = Math.max(20, rightLimit - 4);
		if (this.font.width(desc) > descMax)
			desc = this.font.plainSubstrByWidth(desc, descMax - this.font.width("…")) + "…";
		int descX = Math.max(4, this.width / 2 - this.font.width(desc) / 2);
		if (descX + this.font.width(desc) > rightLimit)
			descX = Math.max(4, rightLimit - this.font.width(desc));
		graphics.drawString(this.font, desc, descX, 22, serverConfigLocked ? 0xFF5555 : 0xAAAAAA);

		// CUSTOM current values
		if (selected == Difficulty.CUSTOM) {
			int cw = this.width / 2;
			graphics.drawString(this.font, chanceLabel(), cw - 168, 58, 0x55FFFF);
			graphics.drawString(this.font, timeLabel(), cw + 94, 58, 0x55FF55);
		}

		// Recipe list header + rows
		graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.per_recipe"), this.width / 2, LIST_Y_TOP - 12, 0xFFFF55);

		int listBottom = this.height - 62;
		if (recipes.isEmpty()) {
			graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.enter_world"), this.width / 2, LIST_Y_TOP + 20, 0x888888);
		} else {
			graphics.fill(LIST_X, LIST_Y_TOP, this.width - LIST_X, listBottom, 0x30000000);
			graphics.enableScissor(LIST_X, LIST_Y_TOP, this.width - LIST_X, listBottom);
			int rows = visibleRows();
			for (int row = 0; row < rows; row++) {
				int idx = row + scroll;
				if (idx >= recipes.size()) break;
				SiftingRecipe r = recipes.get(idx);
				int y = LIST_Y_TOP + row * ROW_HEIGHT;
				if ((row & 1) == 0)
					graphics.fill(LIST_X, y, this.width - LIST_X, y + ROW_HEIGHT - 2, 0x11000000);
				// Recipe identity: [input icon] input name [mesh icon] · summary.
				// The input (sifted item) is what the player matches in-game, so it leads the row.
				ItemStack siftable = r.getSiftableItemStack();
				ItemStack mesh = r.getMeshItemStack();
				int cursor = LIST_X + 22;
				if (!siftable.isEmpty()) {
					graphics.renderItem(siftable, cursor, y + 1);
					cursor += 20;
				}
				String inName = siftable.isEmpty() ? r.getId().getPath() : siftable.getHoverName().getString();
				// Cap the names so the output summary always keeps room; long CJK/ASCII names are ellipsized.
				int nameCap = Math.max(48, this.width / 4);
				if (this.font.width(inName) > nameCap)
					inName = this.font.plainSubstrByWidth(inName, nameCap - this.font.width("…")) + "…";
				graphics.drawString(this.font, inName, cursor, y + 1, siftable.isEmpty() ? 0xAAAAAA : 0x55FFFF);
				cursor += this.font.width(inName) + 4;
				if (!mesh.isEmpty()) {
					graphics.renderItem(mesh, cursor, y + 1);
					cursor += 20;
					String meshName = mesh.getHoverName().getString();
					int meshCap = Math.max(40, this.width / 4);
					if (this.font.width(meshName) > meshCap)
						meshName = this.font.plainSubstrByWidth(meshName, meshCap - this.font.width("…")) + "…";
					graphics.drawString(this.font, meshName, cursor, y + 1, 0x55FF55);
					cursor += this.font.width(meshName) + 4;
				}
				cursor += 2;
				String summary = recipeSummary(r);
				// Trim to the actual remaining pixel width (no CJK/ASCII mixing surprises).
				int maxW = (this.width - LIST_X) - cursor - 4;
				if (this.font.width(summary) > maxW && maxW > 10)
					summary = this.font.plainSubstrByWidth(summary, maxW - this.font.width("…")) + "…";
				graphics.drawString(this.font, summary, cursor, y + 1, 0xFFFFFF);
			}
			graphics.disableScissor();
			// Scrollbar
			if (recipes.size() > rows) {
				int barH = Math.max(10, (listBottom - LIST_Y_TOP) * rows / recipes.size());
				int barY = LIST_Y_TOP + (int) ((listBottom - LIST_Y_TOP - barH) * (double) scroll / Math.max(1, recipes.size() - rows));
				graphics.fill(this.width - 16, barY, this.width - 14, barY + barH, 0xFFAAAAAA);
			}
		}

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	private String describe(Difficulty d) {
		switch (d) {
		case ULTRA: return Component.translatable("createsifter.config.preset.ultra.desc").getString();
		case HIGH: return Component.translatable("createsifter.config.preset.high.desc").getString();
		case MEDIUM: return Component.translatable("createsifter.config.preset.medium.desc").getString();
		case LOW: return Component.translatable("createsifter.config.preset.low.desc").getString();
		default: return Component.translatable("createsifter.config.preset.custom.desc").getString();
		}
	}

	private static String pct(float v) {
		return Math.round(v * 100) + "%";
	}

	private static String f(double v) {
		return String.format(Locale.ROOT, "%.2f", v);
	}

	/** "Chance 60%" style label; used both by render() and by the stepper placement. */
	private String chanceLabel() {
		return Component.translatable("createsifter.config.chance").getString() + " " + pct(SifterConfig.customChanceMultiplier());
	}

	/** "Time 1.50x" style label; used both by render() and by the stepper placement. */
	private String timeLabel() {
		return Component.translatable("createsifter.config.time").getString() + " " + f(SifterConfig.customTimeMultiplier()) + "x";
	}

	@Override
	public void onClose() {
		// Done / Esc: drop *player-created* recipes that can never work (empty
		// siftable or mesh, no resolved outputs, or JSON that failed to
		// deserialise) so broken drafts simply disappear. Entries whose id is a
		// built-in recipe id are manual server-admin edits and are kept.
		ClientConfigWrites.execute(() -> {
			for (String id : SifterConfig.customRecipes().keySet()) {
				if (!id.startsWith(CustomRecipeStore.CUSTOM_PREFIX))
					continue;
				SiftingRecipe r = CustomRecipeStore.instances().get(id);
				if (r == null || !isUsable(r))
					SifterConfig.removeCustomRecipe(id);
			}
		});
		this.minecraft.setScreen(parent);
	}

	private static boolean isUsable(SiftingRecipe r) {
		if (r.getSiftableItemStack().isEmpty()) return false;
		if (r.getMeshItemStack().isEmpty()) return false;
		List<com.simibubi.create.content.processing.recipe.ProcessingOutput> outs = r.getRollableResults();
		if (outs.isEmpty()) return false;
		for (com.simibubi.create.content.processing.recipe.ProcessingOutput o : outs)
			if (o.getStack().isEmpty()) return false;
		return true;
	}
}

