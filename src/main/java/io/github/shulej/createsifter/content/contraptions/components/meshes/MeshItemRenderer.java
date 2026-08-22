/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the MIT License.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.meshes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;

import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MeshItemRenderer extends CustomRenderedItemModelRenderer {
	@Override
	protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		LocalPlayer player = Minecraft.getInstance().player;
		float partialTicks = AnimationTickHolder.getPartialTicks();

		boolean leftHand = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
		boolean firstPerson = leftHand || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

		CompoundTag tag = stack.getOrCreateTag();
		boolean jeiMode = tag.contains("JEI");

		ms.pushPose();

		if (tag.contains("Sifting")) {
			ms.pushPose();

			if (transformType == ItemDisplayContext.GUI) {
				ms.translate(0.0F, .2f, 1.0F);
				ms.scale(.75f, .75f, .75f);
			} else {
				int modifier = leftHand ? -1 : 1;
				ms.mulPose(Axis.YP.rotationDegrees(modifier * 40));
			}

			// Player can be absent (JEI render, main-menu previews, ...); fall back
			// to the jei-mode clock so the sifting pose stays animated regardless.
			float time;
			if (jeiMode || player == null) {
				time = (float) (-AnimationTickHolder.getTicks()) % stack.getUseDuration() - partialTicks + 1.0F;
			} else {
				time = player.getUseItemRemainingTicks() - partialTicks + 1.0F;
			}
			if (time / (float) stack.getUseDuration() < 0.8F) {
				float bobbing = -Mth.abs(Mth.cos(time / 4.0F * (float) Math.PI) * 0.1F);

				if (transformType == ItemDisplayContext.GUI)
					ms.translate(bobbing, bobbing, 0.0F);
				else
					ms.translate(0.0F, bobbing, 0.0F);
			}

			ItemStack toSift = ItemStack.of(tag.getCompound("Sifting"));
			Level level = player != null ? player.level() : Minecraft.getInstance().level;
			if (level != null)
				itemRenderer.renderStatic(toSift, ItemDisplayContext.NONE, light, overlay, ms, buffer, level, 0);

			ms.popPose();
		}

		if (firstPerson && player != null) {
			int itemInUseCount = player.getUseItemRemainingTicks();
			if (itemInUseCount > 0) {
				int modifier = leftHand ? -1 : 1;
				ms.mulPose(Axis.ZP.rotationDegrees(modifier * 1));
				ms.mulPose(Axis.XP.rotationDegrees(modifier * 1));
				ms.mulPose(Axis.YP.rotationDegrees(modifier * 1));
			}
		}

		itemRenderer.render(stack, ItemDisplayContext.NONE, false, ms, buffer, light, overlay, model.getOriginalModel());

		ms.popPose();
	}
}

