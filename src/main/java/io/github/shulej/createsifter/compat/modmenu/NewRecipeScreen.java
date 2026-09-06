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
import io.github.shulej.createsifter.ModRecipeTypes;
import io.github.shulej.createsifter.content.contraptions.components.meshes.MeshTypes;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Editor for a brand-new sifting recipe. Top: a block-only siftable item input
 * and a one-shot mesh picker (icon + name, locked after selection). Below: the
 * shared output row editor. Nothing is written until Done, and an incomplete
 * recipe simply disappears when the screen is left.
 *
 * 新建筛分配方界面：顶部为方块型筛分物输入框与一次性筛网选择（图标+名称，选定后
 * 锁定）；下方复用产物行编辑器。完成时才写入配置，未配置完整的配方直接消失。
 */
public class NewRecipeScreen extends SifterRecipeEditorScreen {

	private final String recipeId;
	private ItemStack siftableStack = ItemStack.EMPTY;
	private boolean siftableLocked;
	private boolean siftableInvalid;
	/** True when the rejected input resolved to a real item that simply is not a block. */
	private boolean siftableNotABlock;
	private boolean duplicateInvalid;
	private EditBox siftableEdit;
	private MeshTypes mesh;
	private boolean meshMenuOpen;
	private Button meshButton;
	private final List<Button> meshOptions = new ArrayList<>();
	private final List<MeshTypes> meshOptionTypes = new ArrayList<>();

	public NewRecipeScreen(Screen parent, String recipeId) {
		super(Component.translatable("createsifter.config.new_recipe"), parent);
		this.recipeId = recipeId;
	}

	@Override
	protected int rowsTop() {
		return 90;
	}

	@Override
	protected int rowsBottom() {
		return this.height - 30;
	}

	@Override
	protected void buildExtras() {
		boolean locked = !canEdit();

		// A resize re-runs init(): clear any leftover dropdown state (the widgets
		// themselves are dropped by init's clearWidgets) and re-apply the two
		// one-shot locks so nothing editable silently reappears.
		clearMeshOptions();
		meshMenuOpen = false;

		int bx = blockX();

		// Siftable item input (block-only, creative-search style). Once confirmed
		// it stays locked: a resize must not bring the editable box back.
		if (!siftableLocked) {
			int labelW = this.font.width(Component.translatable("createsifter.config.siftable").getString()) + 8;
			siftableEdit = new EditBox(this.font, bx + labelW, 26, 220, 16, Component.literal(""));
			siftableEdit.setMaxLength(80);
			siftableEdit.setHint(Component.translatable("createsifter.config.enter_item"));
			siftableEdit.setResponder(t -> { siftableInvalid = false; siftableNotABlock = false; });
			siftableEdit.setEditable(!locked);
			addRenderableWidget(siftableEdit);
		}

		// Mesh picker (one-shot dropdown, locked after selection).
		int meshLabelW = this.font.width(Component.translatable("createsifter.config.mesh").getString()) + 8;
		meshButton = Button.builder(
				Component.translatable("createsifter.config.select_mesh"),
				b -> toggleMeshMenu()).bounds(bx + meshLabelW, 52, 170, 18).build();
		meshButton.active = !locked;
		if (mesh != null) {
			// Re-apply the already-made selection after a resize.
			meshButton.setMessage(Component.literal(meshName(mesh)));
			meshButton.active = false;
		}
		addRenderableWidget(meshButton);

		// Done: commits and returns.
		Button done = Button.builder(
				Component.translatable("createsifter.config.done"),
				b -> this.onClose()).bounds(bx, this.height - 26, 80, 18).build();
		done.active = !locked;
		addRenderableWidget(done);

		// Cancel: leaves without writing anything.
		Button cancel = Button.builder(
				Component.translatable("createsifter.config.cancel"),
				b -> this.minecraft.setScreen(parent)).bounds(bx + 86, this.height - 26, 60, 18).build();
		cancel.active = !locked;
		addRenderableWidget(cancel);
	}

	private String meshName(MeshTypes m) {
		return new ItemStack(m.getItem()).getHoverName().getString();
	}

	private void confirmSiftable() {
		ItemParseHelper.BlockResult result = ItemParseHelper.parseBlockRequired(siftableEdit.getValue());
		if (result.stack().isEmpty()) {
			siftableInvalid = true;
			siftableNotABlock = result.foundNonBlock();
			return;
		}
		siftableStack = result.stack();
		siftableLocked = true;
		removeWidget(siftableEdit);
	}

	private void toggleMeshMenu() {
		if (mesh != null) return; // locked after selection
		meshMenuOpen = !meshMenuOpen;
		if (meshMenuOpen) {
			// The dropdown overlaps the output rows below; take the row widgets out
			// of the click chain so clicks in the overlap always land on a mesh
			// option (1.20.1 dispatches clicks in widget add-order, so visible row
			// controls added earlier would otherwise win). The rows still render.
			hideRowWidgets();
			buildMeshOptions();
		} else {
			closeMeshMenu();
		}
	}

	private void buildMeshOptions() {
		clearMeshOptions();
		int x = meshButton.getX();
		int y = meshButton.getY() + meshButton.getHeight() + 2;
		MeshTypes[] all = MeshTypes.values();
		for (int i = 0; i < all.length; i++) {
			final MeshTypes m = all[i];
			int oy = y + i * 15;
			Button b = new ColoredButton(x, oy, 170, 15, Component.literal(meshName(m)),
					btn -> selectMesh(m), 0xFFFFFF);
			meshOptions.add(b);
			meshOptionTypes.add(m);
			addRenderableWidget(b);
		}
	}

	private void closeMeshMenu() {
		meshMenuOpen = false;
		clearMeshOptions();
		// Bring the hidden row widgets (and the add-product button) back.
		if (addProductButton != null) {
			addProductButton.active = canEdit();
			addRenderableWidget(addProductButton);
		}
		rebuildRows();
	}

	/** Temporarily remove the row controls from the widget list (visuals stay drawn). */
	private void hideRowWidgets() {
		for (Button b : rowButtons) removeWidget(b);
		for (EditBox e : rowInputs) removeWidget(e);
		rowButtons.clear();
		rowInputs.clear();
		if (addProductButton != null) removeWidget(addProductButton);
	}

	private void selectMesh(MeshTypes m) {
		mesh = m;
		duplicateInvalid = false;
		meshMenuOpen = false;
		clearMeshOptions();
		meshButton.setMessage(Component.literal(meshName(m)));
		meshButton.active = false;
		if (addProductButton != null) {
			addProductButton.active = canEdit();
			addRenderableWidget(addProductButton);
		}
		rebuildRows();
	}

	private void clearMeshOptions() {
		for (Button b : meshOptions) removeWidget(b);
		meshOptions.clear();
		meshOptionTypes.clear();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (meshMenuOpen) {
			// Scrolling closes the dropdown first: its options overlap the row
			// area, and rebuilding rows while it is open would re-introduce the
			// click conflict it is designed to avoid.
			closeMeshMenu();
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		// Esc always leaves without committing: a new recipe is only saved via
		// Done, so a duplicate-recipe rejection can never trap the player here.
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.minecraft.setScreen(parent);
			return true;
		}
		if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
				&& siftableEdit != null && siftableEdit.isFocused() && !siftableLocked) {
			confirmSiftable();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	/**
	 * Done. Writes the recipe only when it is complete (block siftable,
	 * a mesh and at least one resolved output); otherwise it disappears.
	 * Esc leaves without committing (see keyPressed).
	 */
	@Override
	protected void commit() {
		if (!canEdit()) return;
		validRows();
		if (siftableStack.isEmpty() || mesh == null || rows.isEmpty()) {
			this.minecraft.setScreen(parent);
			return;
		}
		if (duplicateExists()) {
			duplicateInvalid = true;
			return; // stay on the screen and show the warning
		}
		ClientConfigWrites.execute(() -> {
			JsonObject json = new JsonObject();
			JsonArray ingredients = new JsonArray();
			ingredients.add(Ingredient.of(siftableStack).toJson());
			ingredients.add(Ingredient.of(new ItemStack(mesh.getItem())).toJson());
			json.add("ingredients", ingredients);
			JsonArray results = new JsonArray();
			for (OutputRow r : rows)
				// Write the live UI chance/count, matching the existing-recipe editor.
				results.add(makeProcessingOutput(r.output.getStack(), r.chancePercent, r.count).serialize());
			json.add("results", results);
			json.addProperty("processingTime", 500);
			json.addProperty("waterlogged", false);
			json.addProperty("minimumSpeed", SifterConfig.SIFTER_MINIMUM_SPEED.get().floatValue());
			SifterConfig.putCustomRecipe(recipeId, json);
			SifterConfig.clearOverrides(recipeId);
		});
		this.minecraft.setScreen(parent);
	}

	/**
	 * True when another recipe (built-in or player-made) already sifts the same
	 * block with the same mesh, which would make the match outcome ambiguous.
	 */
	private boolean duplicateExists() {
		Level level = this.minecraft == null ? null : this.minecraft.level;
		if (level == null) return false;
		ItemStack meshStack = new ItemStack(mesh.getItem());
		for (SiftingRecipe r : ModRecipeTypes.customizedSiftingRecipes(level)) {
			ItemStack s = r.getSiftableItemStack();
			ItemStack m = r.getMeshItemStack();
			if (!s.isEmpty() && !m.isEmpty()
					&& ItemStack.isSameItem(s, siftableStack) && ItemStack.isSameItem(m, meshStack))
				return true;
		}
		return false;
	}

	@Override
	protected void addNewProduct() {
		duplicateInvalid = false;
		super.addNewProduct();
	}

	@Override
	protected void removeRow(int outputIndex) {
		duplicateInvalid = false;
		super.removeRow(outputIndex);
	}

	@Override
	protected void renderTitle(GuiGraphics graphics) {
		graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.new_recipe"),
				this.width / 2, 10, 0xFFFFFF);

		int bx = blockX();
		int labelW = this.font.width(Component.translatable("createsifter.config.siftable").getString()) + 8;
		graphics.drawString(this.font, Component.translatable("createsifter.config.siftable"), bx, 30, 0x55FFFF);
		if (siftableLocked) {
			graphics.renderItem(siftableStack, bx + labelW, 27);
			String name = siftableStack.getHoverName().getString();
			int maxNameW = this.width - (bx + labelW + 18) - 8;
			if (this.font.width(name) > maxNameW && maxNameW > 8)
				name = this.font.plainSubstrByWidth(name, maxNameW - this.font.width("…")) + "…";
			graphics.drawString(this.font, name, bx + labelW + 18, 30, 0xFFFFFF);
		} else if (siftableInvalid) {
			// Below the input box (y=26..42): at y=30 the EditBox would paint over it.
			// A typed-but-non-block item gets its own hint, otherwise "invalid" alone
			// leaves players guessing why a perfectly valid item was rejected.
			String key = siftableNotABlock ? "createsifter.config.requires_block" : "createsifter.config.invalid_item";
			graphics.drawString(this.font, Component.translatable(key),
					bx + labelW + 8, 44, 0xFF5555);
		}

		int meshLabelW = this.font.width(Component.translatable("createsifter.config.mesh").getString()) + 8;
		graphics.drawString(this.font, Component.translatable("createsifter.config.mesh"), bx, 56, 0x55FF55);
		if (mesh != null)
			graphics.renderItem(new ItemStack(mesh.getItem()), bx + meshLabelW + 2, 53);

		if (duplicateInvalid)
			graphics.drawCenteredString(this.font, Component.translatable("createsifter.config.duplicate_recipe"),
					this.width / 2, this.height - 46, 0xFF5555);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		// Icons for the open mesh dropdown options.
		for (int i = 0; i < meshOptions.size(); i++) {
			Button b = meshOptions.get(i);
			graphics.renderItem(new ItemStack(meshOptionTypes.get(i).getItem()), b.getX() + 2, b.getY());
		}
	}

	@Override
	public void onClose() {
		commit();
	}
}
