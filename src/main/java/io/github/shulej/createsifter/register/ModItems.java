package io.github.shulej.createsifter.register;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;

import io.github.shulej.createsifter.CreateSifter;
import io.github.shulej.createsifter.content.contraptions.components.meshes.BrassMesh;
import io.github.shulej.createsifter.content.contraptions.components.meshes.StringMesh;
import io.github.shulej.createsifter.content.contraptions.components.meshes.AndesiteMesh;
import io.github.shulej.createsifter.content.contraptions.components.meshes.ZincMesh;
import io.github.shulej.createsifter.content.contraptions.components.meshes.CustomMesh;
import io.github.shulej.createsifter.content.contraptions.components.meshes.AdvancedBrassMesh;
import io.github.shulej.createsifter.content.contraptions.components.meshes.AdvancedCustomMesh;

public class ModItems {
	static {
		CreateSifter.REGISTRATE.setCreativeTab(ModCreativeTabs.MAIN_KEY);
	}
	public static void register() {}

	public static final ItemEntry<StringMesh> STRING_MESH =
			CreateSifter.REGISTRATE.item("string_mesh", StringMesh::new)
					.properties(p -> p.stacksTo(1))
					.model(AssetLookup.existingItemModel())
					.tag(ModTags.ModItemTags.MESHES.tag)
					.register();
	public static final ItemEntry<AndesiteMesh> ANDESITE_MESH =
			CreateSifter.REGISTRATE.item("andesite_mesh", AndesiteMesh::new)
					.properties(p -> p.stacksTo(1))
					.model(AssetLookup.existingItemModel())
					.tag(ModTags.ModItemTags.MESHES.tag)
					.register();
	public static final ItemEntry<ZincMesh> ZINC_MESH =
			CreateSifter.REGISTRATE.item("zinc_mesh", ZincMesh::new)
					.properties(p -> p.stacksTo(1))
					.model(AssetLookup.existingItemModel())
					.tag(ModTags.ModItemTags.MESHES.tag)
					.register();
	public static final ItemEntry<BrassMesh> BRASS_MESH =
			CreateSifter.REGISTRATE.item("brass_mesh", BrassMesh::new)
					.properties(p -> p.stacksTo(1))
					.model(AssetLookup.existingItemModel())
					.tag(ModTags.ModItemTags.MESHES.tag)
					.register();
	public static final ItemEntry<CustomMesh> CUSTOM_MESH =
			CreateSifter.REGISTRATE.item("custom_mesh", CustomMesh::new)
					.properties(p -> p.stacksTo(1))
					.model(AssetLookup.existingItemModel())
					.tag(ModTags.ModItemTags.MESHES.tag)
					.register();

	public static final ItemEntry<AdvancedBrassMesh> ADVANCED_BRASS_MESH =
			CreateSifter.REGISTRATE.item("advanced_brass_mesh", AdvancedBrassMesh::new)
					.properties(p -> p.stacksTo(1))
					.model(AssetLookup.existingItemModel())
					.tag(ModTags.ModItemTags.MESHES.tag)
					.register();

	public static final ItemEntry<AdvancedCustomMesh> ADVANCED_CUSTOM_MESH =
			CreateSifter.REGISTRATE.item("advanced_custom_mesh", AdvancedCustomMesh::new)
					.properties(p -> p.stacksTo(1))
					.model(AssetLookup.existingItemModel())
					.tag(ModTags.ModItemTags.MESHES.tag)
					.register();
}
