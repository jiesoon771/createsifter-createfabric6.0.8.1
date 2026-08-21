package io.github.shulej.createsifter.register;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import com.electronwill.nightconfig.core.io.WritingMode;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.github.shulej.createsifter.CreateSifter;
import io.github.shulej.createsifter.content.contraptions.components.brass_sifter.BrassSifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.Difficulty;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ModConfigs {
	public static ForgeConfigSpec SERVER;
	public static ForgeConfigSpec COMMON;
	private static CommentedFileConfig SERVER_CONFIG;
	private static CommentedFileConfig COMMON_CONFIG;

	public static void register() {
		registerServerConfigs();
		registerCommonConfigs();
		registerClientConfigs();
	}

	private static void registerClientConfigs() {
		ForgeConfigSpec.Builder	CLIENT_BUILDER = new ForgeConfigSpec.Builder();
		SifterConfig.registerClientConfig(CLIENT_BUILDER);
		BrassSifterConfig.registerClientConfig(CLIENT_BUILDER);
		ForgeConfigRegistry.INSTANCE.register(CreateSifter.MODID, ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
	}

	private static void registerCommonConfigs() {
		ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		SifterConfig.registerCommonConfig(COMMON_BUILDER);
		BrassSifterConfig.registerCommonConfig(COMMON_BUILDER);
		COMMON = COMMON_BUILDER.build();
		ForgeConfigRegistry.INSTANCE.register(CreateSifter.MODID, ModConfig.Type.COMMON, COMMON);
		COMMON_CONFIG = ModConfigs.loadConfig(COMMON, FabricLoader.getInstance().getConfigDir().resolve(CreateSifter.MODID + "-common.toml"));
	}

	private static void registerServerConfigs() {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		SifterConfig.registerServerConfig(SERVER_BUILDER);
		SERVER = SERVER_BUILDER.build();
		ForgeConfigRegistry.INSTANCE.register(CreateSifter.MODID, ModConfig.Type.SERVER, SERVER);
		SERVER_CONFIG = ModConfigs.loadConfig(SERVER, FabricLoader.getInstance().getConfigDir().resolve(CreateSifter.MODID + "-server.toml"));
	}

	/** Switch the difficulty preset at runtime and persist it to the server config file. */
	public static void setDifficulty(Difficulty difficulty) {
		SifterConfig.DIFFICULTY.set(difficulty);
		saveServerConfig();
	}

	/** Replace the whole recipe override list at runtime and persist it. */
	public static void setRecipeOverrides(java.util.List<? extends String> overrides) {
		SifterConfig.RECIPE_OVERRIDES.set(overrides);
		saveServerConfig();
	}

	private static void saveServerConfig() {
		if (SERVER_CONFIG != null) {
			SERVER_CONFIG.save();
		}
	}

	/** Persist any pending server-config changes to disk. */
	public static void saveServer() {
		saveServerConfig();
	}

	public static CommentedFileConfig loadConfig(ForgeConfigSpec spec, java.nio.file.Path path) {
		final CommentedFileConfig configData = CommentedFileConfig.builder(path)
				.sync()
				.autosave()
				.writingMode(WritingMode.REPLACE)
				.build();
		configData.load();
		spec.setConfig(configData);
		return configData;
	}
}