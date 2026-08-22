/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the MIT License.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.sifter;

/**
 * Difficulty presets for sifting recipes.
 *
 * ULTRA / HIGH / MEDIUM / LOW adjust the output chance multiplier and the
 * processing time multiplier applied to every sifting recipe. HIGH is the
 * original (vanilla) behaviour. CUSTOM reads both multipliers from the live
 * server config values. The active preset is stored in the server config
 * ("difficulty") and can be changed from the Mod Menu config screen.
 */
public enum Difficulty {
	ULTRA("ultra", 1.5f, 0.5f),
	HIGH("high", 1.0f, 1.0f),
	MEDIUM("medium", 0.6f, 1.5f),
	LOW("low", 0.35f, 2.0f),
	CUSTOM("custom", 1.0f, 1.0f);

	public final String key;
	/** Multiplier applied to every output chance (clamped to 1.0). */
	public final float chanceMultiplier;
	/** Multiplier applied to the machine processing duration (>= 1 tick). */
	public final float timeMultiplier;

	Difficulty(String key, float chanceMultiplier, float timeMultiplier) {
		this.key = key;
		this.chanceMultiplier = chanceMultiplier;
		this.timeMultiplier = timeMultiplier;
	}

	public static Difficulty current() {
		return SifterConfig.DIFFICULTY.get();
	}

	/** Effective chance multiplier, resolving CUSTOM from the live config values. */
	public static float effectiveChanceMultiplier() {
		Difficulty d = current();
		return d == CUSTOM ? SifterConfig.customChanceMultiplier() : d.chanceMultiplier;
	}

	/** Effective time multiplier, resolving CUSTOM from the live config values. */
	public static float effectiveTimeMultiplier() {
		Difficulty d = current();
		return d == CUSTOM ? SifterConfig.customTimeMultiplier() : d.timeMultiplier;
	}
}
