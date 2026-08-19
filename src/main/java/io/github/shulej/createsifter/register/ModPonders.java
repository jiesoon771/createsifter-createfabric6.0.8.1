package io.github.shulej.createsifter.register;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

import io.github.shulej.createsifter.CreateSifter;
import io.github.shulej.createsifter.ponders.PonderScenes;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class ModPonders implements PonderPlugin {

	@Override
	public String getModId() {
		return CreateSifter.MODID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

		HELPER.forComponents(ModBlocks.SIFTER_BLOCK, ModBlocks.BRASS_SIFTER_BLOCK)
			.addStoryBoard("sifter", PonderScenes::sifter, AllCreatePonderTags.KINETIC_APPLIANCES);
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
		PonderTagRegistrationHelper<ItemProviderEntry<?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

		HELPER.addTagToComponent(ModBlocks.SIFTER_BLOCK, AllCreatePonderTags.KINETIC_APPLIANCES);
		HELPER.addTagToComponent(ModBlocks.BRASS_SIFTER_BLOCK, AllCreatePonderTags.KINETIC_APPLIANCES);
	}
}
