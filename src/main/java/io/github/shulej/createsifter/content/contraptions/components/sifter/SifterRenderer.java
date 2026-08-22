/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the MIT License.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.sifter;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;

import io.github.shulej.createsifter.register.ModPartials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SifterRenderer extends KineticBlockEntityRenderer<SifterBlockEntity> {
	public SifterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(SifterBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		ItemStack meshItemStack = blockEntity.meshInv.getStackInSlot(0);
		double xPos = 0.0;
		if (SifterConfig.SIFTER_RENDER_MOVING_MESH.get()) {
			xPos = Math.sin(blockEntity.getProgress())/40;
		}

		if (!meshItemStack.isEmpty()) {
			ms.pushPose();
			TransformStack.of(ms).translate(new Vec3(0.5 - xPos, 1.51, 0.5));
			renderStaticBlock(ms, buffer, light, overlay, meshItemStack, blockEntity);
			ms.popPose();

			ItemStack inProcessItemStack = blockEntity.getInputItemStack();
			if (!inProcessItemStack.equals(ItemStack.EMPTY) && SifterConfig.SIFTER_RENDER_SIFTED_BLOCK.get()) {
				float progress = blockEntity.getProcessingRemainingPercent();
				ms.pushPose();
				TransformStack.of(ms)
						.scale((float) .9, progress, (float) .9)
						.translate(new Vec3(-xPos + 0.05, 1.05 / progress, 0.05));
				renderBlockFromItemStack(inProcessItemStack, ms, buffer, light, overlay);
				ms.popPose();
			}
		}
		super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderStaticBlock(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack itemStack, SifterBlockEntity blockEntity) {
		Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemDisplayContext.NONE, light, overlay, ms, buffer, blockEntity.getLevel(), 0);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(SifterBlockEntity blockEntity, BlockState state) {
		return CachedBuffers.partial(ModPartials.SIFTER_COG, state);
	}

	protected void renderBlockFromItemStack(ItemStack itemStack,PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		Item item = itemStack.getItem();
		BlockState blockState = Blocks.AIR.defaultBlockState();
		if(item instanceof BlockItem){
			blockState = ((BlockItem) item).getBlock().defaultBlockState();
		}

		Minecraft.getInstance()
				.getBlockRenderer()
				.renderSingleBlock(blockState, ms,buffer,light,overlay);
	}
}

