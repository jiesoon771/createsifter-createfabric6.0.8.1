package io.github.shulej.createsifter;

import net.createmod.catnip.lang.Lang;

import io.github.fabricators_of_create.porting_lib.util.ShapedRecipeUtil;
import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipeSerializer;
import io.github.shulej.createsifter.foundation.data.recipe.SiftingRecipeBuilder;
import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import io.github.shulej.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum ModRecipeTypes implements IRecipeTypeInfo {
	SIFTING(SiftingRecipe::new);

	private final ResourceLocation id;
	private final RecipeSerializer<?> serializerObject;
	@Nullable
	private final RecipeType<?> typeObject;
	private final Supplier<RecipeType<?>> type;

	ModRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier, Supplier<RecipeType<?>> typeSupplier, boolean registerType) {
		String name = Lang.asId(name());
		id = CreateSifter.asResource(name);
		serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		if (registerType) {
			typeObject = typeSupplier.get();
			Registry.register(BuiltInRegistries.RECIPE_TYPE, id, typeObject);
		} else {
			typeObject = null;
		}
		type = typeSupplier;
	}
	ModRecipeTypes(Supplier<RecipeSerializer<?>> serializerSupplier) {
		String name = Lang.asId(name());
		id = CreateSifter.asResource(name);
		serializerObject = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializerSupplier.get());
		typeObject = simpleType(id);
		Registry.register(BuiltInRegistries.RECIPE_TYPE, id, typeObject);
		type = () -> typeObject;
	}

	ModRecipeTypes(SiftingRecipeBuilder.SiftingRecipeFactory processingFactory) {
		this(() -> new SiftingRecipeSerializer(processingFactory));
	}

	public static <T extends Recipe<?>> RecipeType<T> simpleType(ResourceLocation id) {
		String stringId = id.toString();
		return new RecipeType<T>() {
			@Override
			public String toString() {
				return stringId;
			}
		};
	}

	public static void register() {
		ShapedRecipeUtil.setCraftingSize(9, 9);
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeSerializer<?>> T getSerializer() {
		return (T) serializerObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends RecipeType<?>> T getType() {
		return (T) type.get();
	}

	public <C extends Container, T extends Recipe<C>> Optional<T> find(C inv, Level world) {
		return world.getRecipeManager()
				.getRecipeFor(getType(), inv, world);
	}

	public Optional<SiftingRecipe> find(Container inv, Level world, boolean waterlogged, float speed) {
		if (world.isClientSide)
			return Optional.empty();
		List<SiftingRecipe> siftingRecipes = world.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SIFTING.getType());
		Stream<SiftingRecipe> siftingRecipesFiltered = siftingRecipes.stream().filter(siftingRecipe -> siftingRecipe.matches(inv, world, waterlogged, speed, false));
		return siftingRecipesFiltered.findAny();
	}

	public Optional<SiftingRecipe> findAdvanced(Container inv, Level world, boolean waterlogged, float speed) {
		if (world.isClientSide)
			return Optional.empty();
		List<SiftingRecipe> siftingRecipes = world.getRecipeManager().getAllRecipesFor(ModRecipeTypes.SIFTING.getType());
		Stream<SiftingRecipe> siftingRecipesFiltered = siftingRecipes.stream().filter(siftingRecipe -> siftingRecipe.matches(inv, world, waterlogged, speed, true));
		return siftingRecipesFiltered.findAny();
	}

	public static boolean shouldIgnoreAutomation(Recipe<?> recipe) {
		RecipeSerializer<?> serializer = recipe.getSerializer();
		return recipe.getId().getPath().endsWith("_manual_only");
	}
}
