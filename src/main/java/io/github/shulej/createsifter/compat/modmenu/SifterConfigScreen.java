package io.github.shulej.createsifter.compat.modmenu;

import io.github.shulej.createsifter.ModRecipeTypes;
import io.github.shulej.createsifter.content.contraptions.components.sifter.Difficulty;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import io.github.shulej.createsifter.register.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

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
	private final List<SiftingRecipe> recipes = new ArrayList<>();
	private boolean serverConfigLocked;
	private Button doneButton;

	private static final int LIST_X = 12;
	private static final int LIST_Y_TOP = 78;
	private static final int ROW_HEIGHT = 22;

	public SifterConfigScreen(Screen parent) {
		super(Component.translatable("createsifter.config.title"));
		this.parent = parent;
		this.selected = Difficulty.current();
	}

	@Override
	protected void init() {
		this.clearWidgets();
		Minecraft mc = Minecraft.getInstance();
		// A single-player (integrated) server shares this JVM's config objects, so
		// edits apply. Anything that runs a separate server process cannot be edited.
		this.serverConfigLocked = mc.getCurrentServer() != null && !mc.hasSingleplayerServer();

		collectRecipes();

		// --- Difficulty preset row (5 buttons) ---
		Difficulty[] presets = new Difficulty[]{Difficulty.ULTRA, Difficulty.HIGH, Difficulty.MEDIUM, Difficulty.LOW, Difficulty.CUSTOM};
		int presetW = 66;
		int presetGap = 4;
		int presetTotal = presets.length * presetW + (presets.length - 1) * presetGap;
		int bx = this.width / 2 - presetTotal / 2;
		int i = 0;
		for (Difficulty d : presets) {
			final Difficulty dd = d;
			Button b = Button.builder(presetLabel(d), b2 -> choose(dd)).bounds(bx + i * (presetW + presetGap), 34, presetW, 20).build();
			if (serverConfigLocked)
				b.active = false;
			addRenderableWidget(b);
			i++;
		}

		// --- CUSTOM steppers ---
		if (selected == Difficulty.CUSTOM && !serverConfigLocked) {
			int cw = this.width / 2;
			addRenderableWidget(Button.builder(Component.literal("<"), b -> stepChance(-0.05f)).bounds(cw - 150, 56, 18, 18).build());
			addRenderableWidget(Button.builder(Component.literal(">"), b -> stepChance(+0.05f)).bounds(cw - 128, 56, 18, 18).build());
			addRenderableWidget(Button.builder(Component.literal("<"), b -> stepTime(-0.05f)).bounds(cw + 96, 56, 18, 18).build());
			addRenderableWidget(Button.builder(Component.literal(">"), b -> stepTime(+0.05f)).bounds(cw + 114, 56, 18, 18).build());
		}

		// --- Done button (fixed at bottom) ---
		doneButton = Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
				.bounds(this.width / 2 - 80, this.height - 28, 160, 20).build();
		addRenderableWidget(doneButton);

		rebuildRecipeList();
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
		return sb.toString();
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
		ModConfigs.setDifficulty(d);
		this.selected = d;
		// Rebuild to show/hide CUSTOM steppers and refresh the preset buttons.
		init();
	}

	private void stepChance(float delta) {
		if (serverConfigLocked) return;
		double v = SifterConfig.customChanceMultiplier() + delta;
		SifterConfig.CUSTOM_CHANCE_MULTIPLIER.set(Math.max(0.05, Math.min(1.0, v)));
		ModConfigs.saveServer();
	}

	private void stepTime(float delta) {
		if (serverConfigLocked) return;
		double v = SifterConfig.customTimeMultiplier() + delta;
		SifterConfig.CUSTOM_TIME_MULTIPLIER.set(Math.max(0.1, Math.min(10.0, v)));
		ModConfigs.saveServer();
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

		// Preset description, with the server-wins note appended on the same line.
		String note = serverConfigLocked
				? Component.translatable("createsifter.config.readonly_multiplayer").getString()
				: Component.translatable("createsifter.config.server_wins").getString();
		graphics.drawCenteredString(this.font, describe(selected) + "  ·  " + note, this.width / 2, 22, serverConfigLocked ? 0xFF5555 : 0xAAAAAA);

		// CUSTOM current values
		if (selected == Difficulty.CUSTOM) {
			int cw = this.width / 2;
			graphics.drawString(this.font, Component.translatable("createsifter.config.chance") + " " + pct(SifterConfig.customChanceMultiplier()), cw - 168, 58, 0x55FFFF);
			graphics.drawString(this.font, Component.translatable("createsifter.config.time") + " " + f(SifterConfig.customTimeMultiplier()) + "x", cw + 94, 58, 0x55FF55);
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

	private static String f(float v) {
		return String.format("%,.2f", v).replace(',', '.');
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(parent);
	}
}
