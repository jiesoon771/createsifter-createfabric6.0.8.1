/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.register;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;

import com.simibubi.create.foundation.data.recipe.Mods;
import net.createmod.catnip.lang.Lang;

import com.tterrag.registrate.providers.ProviderType;

import io.github.shulej.createsifter.CreateSifter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ModTags {

	static {
		CreateSifter.REGISTRATE.setCreativeTab(ModCreativeTabs.MAIN_KEY);
	}

	public static <T> TagKey<T> optionalTag(Registry<T> registry, ResourceLocation id) {
		return TagKey.create(registry.key(), id);
	}

	public enum NameSpace {
		MOD(CreateSifter.MODID, false, true), CREATE(Create.ID, true, false);
		public final String id;
		public final boolean optionalDefault;
		public final boolean alwaysDatagenDefault;

		NameSpace(String id) {
			this(id, true, false);
		}

		NameSpace(String id, boolean optionalDefault, boolean alwaysDatagenDefault) {
			this.id = id;
			this.optionalDefault = optionalDefault;
			this.alwaysDatagenDefault = alwaysDatagenDefault;
		}
	}
	public enum ModItemTags {
		MESHES;
		public final TagKey<Item> tag;

		ModItemTags() {
			this(NameSpace.MOD);
		}

		ModItemTags(NameSpace namespace) {
			this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
		}

		ModItemTags(NameSpace namespace, String path) {
			this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
		}

		ModItemTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
			this(namespace, null, optional, alwaysDatagen);
		}

		ModItemTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
			ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
			if (optional )tag = optionalTag(BuiltInRegistries.ITEM, id);
			else tag = TagKey.create(Registries.ITEM, id);
			if (alwaysDatagen) {
				CreateSifter.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.addTag(tag));
			}
		}
		@SuppressWarnings("deprecation")
		public boolean matches(Item item) {
			return item.builtInRegistryHolder().is(tag);
		}

		public boolean matches(ItemStack stack) {
			return stack.is(tag);
		}

		public void add(Item... values) {
			CreateSifter.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.addTag(tag).add(values));
		}

		public void addOptional(Mods mod, String... ids) {
			CreateSifter.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> {
				TagsProvider.TagAppender<Item> builder = prov.addTag(tag);
				for (String id : ids) {
					builder.addOptional(mod.asResource(id));
				}
			});
		}

		public void includeIn(TagKey<Item> parent) {
			CreateSifter.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.addTag(parent).addTag(tag));
		}

		public void includeIn(AllTags.AllItemTags parent) {
			this.includeIn(parent.tag);
		}

		public void includeAll(TagKey<Item> child) {
			CreateSifter.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.addTag(tag).addTag(child));
		}
	}

	public static void register() {

	}
}
