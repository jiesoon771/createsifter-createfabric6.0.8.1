package io.github.shulej.createsifter.compat.modmenu;

import io.github.shulej.createsifter.ModRecipeTypes;
import io.github.shulej.createsifter.content.contraptions.components.sifter.Difficulty;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeManager;

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
	private Button doneButton;

	private static final int LIST_X = 12;
	private static final int LIST_Y_TOP = 78;
	private static final int ROW_HEIGHT = 22;

	// Per-frame work in render() is limited to what must change each frame:
	// the per-recipe summaries are cached and rebuilt only when the override
	// table changes (single-player edits go through RecipeEditScreen).
	private int summaryGen = -1;
	private final Map<SiftingRecipe, String> summaryCache = new HashMap<>();

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

		collectRecipes();
		buildHeader();
		rebuildRecipeList();
	}

	private void buildHeader() {
		for (Button b : headerButtons) removeWidget(b);
		headerButtons.clear();

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
			b.active = !serverConfigLocked;
			headerButtons.add(b);
			addRenderableWidget(b);
			i++;
		}

		// --- CUSTOM steppers (chance steps by 5 percent, time by 0.1) ---
		if (selected == Difficulty.CUSTOM && !serverConfigLocked) {
			int cw = this.width / 2;
			headerButtons.add(Button.builder(Component.literal("<"), b -> stepChance(-5)).bounds(cw - 150, 56, 18, 18).build());
			headerButtons.add(Button.builder(Component.literal(">"), b -> stepChance(+5)).bounds(cw - 128, 56, 18, 18).build());
			headerButtons.add(Button.builder(Component.literal("<"), b -> stepTime(-0.1)).bounds(cw + 96, 56, 18, 18).build());
			headerButtons.add(Button.builder(Component.literal(">"), b -> stepTime(+0.1)).bounds(cw + 114, 56, 18, 18).build());
			for (Button b : headerButtons.subList(headerButtons.size() - 4, headerButtons.size()))
				addRenderableWidget(b);
		}

		// --- Done button (fixed at bottom) ---
		doneButton = Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
				.bounds(this.width / 2 - 80, this.height - 28, 160, 20).build();
		headerButtons.add(doneButton);
		addRenderableWidget(doneButton);
	}

	private void collectRecipes() {
		recipes.clear();
		Minecraft mc = this.minecraft;
		if (mc == null || mc.level == null || mc.level.getRecipeManager() == null)
			return;
		RecipeManager rm = mc.level.getRecipeManager();
		try {
			recipes.addAll(rm.getAllRecipesFor(ModRecipeTypes.SIFTING.getType()));
			recipes.sort((a, b) -> a.getId().toString().compareTo(b.getId().toString()));
		} catch (Throwable ignored) {
			recipes.clear();
		}
	}

	private int visibleRows() {
		return Math.max(1, (this.height - LIST_Y_TOP - 40) / ROW_HEIGHT);
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
			edit.active = !serverConfigLocked;
			addRenderableWidget(edit);
			editButtons.add(edit);
		}
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
		if (serverConfigLocked) return;
		ClientConfigWrites.execute(() -> SifterConfig.DIFFICULTY.set(d));
		this.selected = d;
		// Only the preset row and CUSTOM steppers depend on the selection; the
		// recipe list is unaffected, so rebuild just the header.
		buildHeader();
	}

	private void stepChance(int deltaPercent) {
		if (serverConfigLocked) return;
		int v = Math.round(SifterConfig.customChanceMultiplier() * 100) + deltaPercent;
		final double clamped = Math.max(5, Math.min(100, v)) / 100.0;
		ClientConfigWrites.execute(() -> SifterConfig.CUSTOM_CHANCE_MULTIPLIER.set(clamped));
	}

	private void stepTime(double delta) {
		if (serverConfigLocked) return;
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
		this.renderBackground(graphics);
		graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.title"), this.width / 2, 12, 0xFFFFFF);

		// Preset description, with the lock reason appended on the same line.
		String note;
		if (serverConfigLocked) {
			note = this.minecraft.level == null
					? Component.translatable("createsifter.config.enter_world").getString()
					: Component.translatable("createsifter.config.readonly_multiplayer").getString();
		} else {
			note = Component.translatable("createsifter.config.server_wins").getString();
		}
		graphics.drawCenteredString(this.font, describe(selected) + "  ·  " + note, this.width / 2, 22, serverConfigLocked ? 0xFF5555 : 0xAAAAAA);

		// CUSTOM current values
		if (selected == Difficulty.CUSTOM) {
			int cw = this.width / 2;
			graphics.drawString(this.font, Component.translatable("createsifter.config.chance").getString() + " " + pct(SifterConfig.customChanceMultiplier()), cw - 168, 58, 0x55FFFF);
			graphics.drawString(this.font, Component.translatable("createsifter.config.time").getString() + " " + f(SifterConfig.customTimeMultiplier()) + "x", cw + 94, 58, 0x55FF55);
		}

		// Recipe list header + rows
		graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.per_recipe"), this.width / 2, LIST_Y_TOP - 12, 0xFFFF55);

		int listBottom = this.height - 40;
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
				String summary = recipeSummary(r);
				// Trim to the actual available pixel width (no CJK/ASCII mixing surprises).
				int maxW = this.width - 2 * LIST_X - 24;
				if (this.font.width(summary) > maxW)
					summary = this.font.plainSubstrByWidth(summary, maxW - this.font.width("…")) + "…";
				graphics.drawString(this.font, summary, LIST_X + 22, y + 1, 0xFFFFFF);
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

	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}
}
