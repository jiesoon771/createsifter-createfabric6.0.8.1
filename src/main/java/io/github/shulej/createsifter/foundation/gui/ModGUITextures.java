/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.foundation.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.ScreenElement;

import net.createmod.catnip.theme.Color;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public enum ModGUITextures implements ScreenElement {
	JEI_SHORT_ARROW("jei/widgets", 20, 9);

	public static final int FONT_COLOR = 5726074;
	public final ResourceLocation location;
	public int width;
	public int height;
	public int startX;
	public int startY;

	private ModGUITextures(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}

	private ModGUITextures(int startX, int startY) {
		this("icons", startX * 16, startY * 16, 16, 16);
	}
	private ModGUITextures(String location, int startX, int startY, int width, int height) {
		this("createsifter", location, startX, startY, width, height);
	}

	private ModGUITextures(String namespace, String location, int startX, int startY, int width, int height) {
		this.location = new ResourceLocation(namespace, "textures/gui/" + location + ".png");
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}

	@Environment(EnvType.CLIENT)
	public void bind() {
		RenderSystem.setShaderTexture(0, this.location);
	}

	@Environment(EnvType.CLIENT)
	public void render(GuiGraphics graphics, int x, int y) {
		graphics.blit(location, x, y, startX, startY, width, height);
	}

	@Environment(EnvType.CLIENT)
	public void render(GuiGraphics graphics, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
	}
}

