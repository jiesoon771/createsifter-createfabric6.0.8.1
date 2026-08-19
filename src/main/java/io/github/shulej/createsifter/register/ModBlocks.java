package io.github.shulej.createsifter.register;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;

import io.github.shulej.createsifter.CreateSifter;
import io.github.shulej.createsifter.content.contraptions.components.brass_sifter.BrassSifterBlock;
import io.github.shulej.createsifter.content.contraptions.components.brass_sifter.BrassSifterConfig;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterBlock;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SifterConfig;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class ModBlocks {
	static {
		CreateSifter.REGISTRATE.setCreativeTab(ModCreativeTabs.MAIN_KEY);
	}

	public static final BlockEntry<SifterBlock> SIFTER_BLOCK = CreateSifter.REGISTRATE.block("sifter", SifterBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.METAL))
			.transform(pickaxeOnly())
			.blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
			.item()
			.transform(customItemModel())
			.register();

	public static final BlockEntry<BrassSifterBlock> BRASS_SIFTER_BLOCK = CreateSifter.REGISTRATE.block("brass_sifter", BrassSifterBlock::new)
			.initialProperties(SharedProperties::stone)
			.properties(p -> p.mapColor(MapColor.METAL))
			.properties(p -> p.noOcclusion())
			.properties(p -> p.isRedstoneConductor((level, pos, state) -> false))
			.transform(pickaxeOnly())
			.blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, AssetLookup.forPowered(c, p)))
			.item()
			.transform(customItemModel())
			.register();

	public static void register() {

	}
}
