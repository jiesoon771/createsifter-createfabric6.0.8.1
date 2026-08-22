/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.compat.jei.category.animations;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.platform.CatnipServices;

import com.tterrag.registrate.util.entry.BlockEntry;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.material.Fluids;

public abstract class BaseAnimatedSifter<SIFTER extends KineticBlock> extends AnimatedKinetics {
	private boolean isWaterlogged = false;

	public BaseAnimatedSifter<SIFTER> waterlogged(boolean value) {
		this.isWaterlogged = value;
		return this;
	}
	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		PoseStack matrixStack = guiGraphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 0);
		AllGuiTextures.JEI_SHADOW.render(guiGraphics, -16, 13);
		matrixStack.translate(-2, 18, 0);
		int scale = 22;

		blockElement(getCogModel())
				.atLocal(0,0.1,0)
				.rotateBlock(22.5, getCurrentAngle() * 2, 0)
				.scale(scale)
				.render(guiGraphics);

		blockElement(getSifterBlock().getDefaultState())
				.atLocal(0,0.1,0)
				.rotateBlock(22.5, 22.5, 0)
				.scale(scale)
				.render(guiGraphics);

		blockElement(getMeshModel())
				.atLocal(0,-1,0)
				.rotateBlock(22.5, 22.5, 0)
				.scale(scale)
				.render(guiGraphics);

		if(isWaterlogged){
			renderWaterlogged(matrixStack);
		}
		matrixStack.popPose();
	}
	private void renderWaterlogged(PoseStack matrixStack){
		AnimatedKinetics.DEFAULT_LIGHTING.applyLighting();
		MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance()
				.getBuilder());
		matrixStack.pushPose();
		UIRenderHelper.flipForGuiRender(matrixStack);
		matrixStack.scale(22, 18, 22);
		matrixStack.translate(-0.3,-0.1,0);
		float from = 1f / 16f;
		float to = 18f / 16f;
		matrixStack.mulPose(Axis.XP.rotationDegrees(22.5f));
		matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
		@SuppressWarnings("unchecked")
		net.createmod.catnip.render.FluidRenderHelper<FluidStack> renderer =
				(net.createmod.catnip.render.FluidRenderHelper<FluidStack>) (Object) CatnipServices.FLUID_RENDERER;
		renderer.renderFluidBox(new FluidStack(Fluids.WATER.getSource(),1000), from, from, from, to, to, to, buffer, matrixStack, LightTexture.FULL_BRIGHT, false, true);
		matrixStack.popPose();
		buffer.endBatch();
		Lighting.setupFor3DItems();
	}

	abstract PartialModel getMeshModel();
	abstract PartialModel getCogModel();
	abstract BlockEntry<SIFTER> getSifterBlock();
}
