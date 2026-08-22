package io.github.shulej.createsifter.register;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import io.github.shulej.createsifter.CreateSifter;
import io.github.shulej.createsifter.content.contraptions.components.brass_sifter.BrassSifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ModConfigs {
	public static ForgeConfigSpec SERVER;
	public static ForgeConfigSpec COMMON;
	private static ModConfig serverModConfig;

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
	}

	private static void registerServerConfigs() {
		ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
		SifterConfig.registerServerConfig(SERVER_BUILDER);
		SERVER = SERVER_BUILDER.build();
		serverModConfig = ForgeConfigRegistry.INSTANCE.register(CreateSifter.MODID, ModConfig.Type.SERVER, SERVER);
	}

	/**
	 * Persist any pending server-config changes to disk.
	 *
	 * The port (Forge Config API Port) owns the single config handle for our
	 * server spec: it loads it on SERVER_STARTING and replaces the spec's backing
	 * config with its own. Writing through that ModConfig keeps exactly one
	 * handle per file - a previous manual loadConfig here created a second
	 * auto-saving handle on the same file, which tore the file apart and made
	 * the port recreate it from defaults (wiping every change).
	 */
	public static void saveServer() {
		if (serverModConfig != null && serverModConfig.getConfigData() != null) {
			try {
				serverModConfig.save();
			} catch (Throwable ignored) {
			}
		}
	}

	/**
	 * Whether the server config is currently attached to a file. This is only
	 * true while a world with a local server is running (single-player or the
	 * dedicated server itself); the title screen and remote servers have no
	 * backing config, so values read as defaults and writes would fail.
	 */
	public static boolean serverConfigLoaded() {
		return serverModConfig != null && serverModConfig.getConfigData() != null;
	}
}
