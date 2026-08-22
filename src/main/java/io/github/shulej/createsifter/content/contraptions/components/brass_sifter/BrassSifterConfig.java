/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the MIT License.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.brass_sifter;

import net.minecraftforge.common.ForgeConfigSpec;

public class BrassSifterConfig {
	public static ForgeConfigSpec.DoubleValue BRASS_SIFTER_STRESS_IMPACT;
	public static ForgeConfigSpec.DoubleValue BRASS_SIFTER_MINIMUM_SPEED;
	public static ForgeConfigSpec.IntValue BRASS_SIFTER_OUTPUT_CAPACITY;
	public static ForgeConfigSpec.IntValue BRASS_SIFTER_ITEMS_PER_CYCLE;

	public static ForgeConfigSpec.BooleanValue BRASS_SIFTER_RENDER_SIFTED_BLOCK;
	public static ForgeConfigSpec.BooleanValue BRASS_SIFTER_RENDER_MOVING_MESH;

	public static void registerCommonConfig(ForgeConfigSpec.Builder COMMON_BUILDER) {
		COMMON_BUILDER.comment("Settings for the mechanical brass sifter").push("brass_sifter");
		BRASS_SIFTER_STRESS_IMPACT = COMMON_BUILDER
				.comment("Stress impact")
				.defineInRange("stressImpact", 8.0, 0.0, 64.0);
		BRASS_SIFTER_MINIMUM_SPEED = COMMON_BUILDER
				.comment("Minimum required speed")
				.defineInRange("minimumSpeed", 16, 0.0, 254);
		BRASS_SIFTER_OUTPUT_CAPACITY = COMMON_BUILDER
				.comment("Output item capacity (applies to newly placed sifters)")
				.defineInRange("outputCapacity", 64, 1, Integer.MAX_VALUE);
		BRASS_SIFTER_ITEMS_PER_CYCLE = COMMON_BUILDER
				.comment("Items processed per cycle")
				.defineInRange("itemsPerCycle", 8, 1, 64);
		COMMON_BUILDER.pop();
	}

	public static void registerClientConfig(ForgeConfigSpec.Builder CLIENT_BUILDER){
		CLIENT_BUILDER.comment("Settings for the mechanical brass sifter").push("brass_sifter");

		BRASS_SIFTER_RENDER_SIFTED_BLOCK = CLIENT_BUILDER
				.comment("Render sifted block").define("renderSiftedBlock",true);

		BRASS_SIFTER_RENDER_MOVING_MESH = CLIENT_BUILDER
				.comment("Render moving mesh").define("renderMovingMesh",true);

		CLIENT_BUILDER.pop();
	}
}

