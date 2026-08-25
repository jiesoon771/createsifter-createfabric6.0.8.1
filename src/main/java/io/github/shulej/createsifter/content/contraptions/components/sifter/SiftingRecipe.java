/*
 * Create Sifting (Fabric port for Minecraft 1.20.1)
 * Copyright (c) 2022 oierbravo
 *
 * This source code is licensed under the GNU Lesser General Public License (LGPL), version 3.
 * See LICENSE.txt for the full license text.
 */
package io.github.shulej.createsifter.content.contraptions.components.sifter;

import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainer;
import io.github.shulej.createsifter.CreateSifter;
import io.github.shulej.createsifter.content.contraptions.components.meshes.AdvancedBaseMesh;
import io.github.shulej.createsifter.content.contraptions.components.meshes.BaseMesh;
import io.github.shulej.createsifter.foundation.data.recipe.SiftingRecipeBuilder;
import io.github.shulej.createsifter.ModRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SiftingRecipe extends AbstractCrushingRecipe {
	/** Shared RNG: rollResults only ever runs on the game thread (server). */
	private static final RandomSource ROLL_RANDOM = RandomSource.create();

	public NonNullList<ProcessingOutput> results;
	private Ingredient siftableIngredient = Ingredient.EMPTY;
	private Ingredient meshIngredient = Ingredient.EMPTY;
	private ItemStack meshStack = ItemStack.EMPTY;
	private ItemStack siftableIngredientStack = ItemStack.EMPTY;
	private boolean waterlogged;
	private float minimumSpeed;
	private boolean advanced;
	/** Game time of the last attempt to re-resolve ingredients that were empty at load. */
	private long lastEmptyResolveAttempt = 0;
	private boolean warnedEmptyIngredients;

	public SiftingRecipe(SiftingRecipeBuilder.SiftingRecipeParams params) {
		super(ModRecipeTypes.SIFTING, params); //change recipe type
		this.processingDuration = params.processingDuration;
		this.ingredients = params.ingredients;
		this.results = params.results;
		this.id = params.id;
		this.waterlogged = params.waterlogged;
		this.minimumSpeed = params.minimumSpeed;
		classifyIngredients();
	}

	@Override
	public SiftingRecipeSerializer getSerializer() {
		return ModRecipeTypes.SIFTING.getSerializer();
	}

	public boolean matches(Container inv, Level worldIn, boolean waterlogged, float speed, boolean advancedMesh) {
		if (inv.isEmpty())
			return false;
		resolveEmptyIngredients(worldIn);
		if(isWaterlogged() != waterlogged)
			return false;
		if(hasSpeedRequirement() && speed < minimumSpeed)
			return false;
		if(advancedMesh && meshStack.getItem() instanceof BaseMesh){
			return false;
		}
		return getSiftableIngredient().test(inv.getItem(0)) && getMeshIngredient().test(inv.getItem(1));
	}

	@Override
	public String toString() {
		return CreateSifter.MODID + ":sifting";
	}
	@Override
	protected int getMaxInputCount() {
		return 4;
	}

	@Override
	protected int getMaxOutputCount() {
		return SifterConfig.SIFTER_OUTPUT_CAPACITY.get();
	}

	@Override
	public boolean matches(Container inv, Level worldIn) {
		return matches(inv, worldIn, false, 0, false);
	}

	/**
	 * Resolve both sides of the recipe once. Ingredients without any matching
	 * item (empty or unresolved tags) are skipped instead of crashing, and the
	 * resolved values are cached so matches() does not re-resolve tags every tick.
	 */
	private void classifyIngredients() {
		for (Ingredient ingredient : ingredients) {
			if (ingredient == null || ingredient.isEmpty())
				continue;
			ItemStack[] stacks = ingredient.getItems();
			if (stacks.length == 0)
				continue;
			if (isMeshItemStack(stacks[0])) {
				if (meshIngredient == Ingredient.EMPTY) {
					meshIngredient = ingredient;
					meshStack = stacks[0];
				}
			} else if (siftableIngredient == Ingredient.EMPTY) {
				siftableIngredient = ingredient;
				siftableIngredientStack = stacks[0];
			}
		}
		this.advanced = isAdvancedMesh(meshStack);
	}

	/**
	 * Ingredients whose tags resolved empty when the recipe loaded are retried once
	 * in a while instead of staying dead forever (tags can arrive from datapacks
	 * that load later). Once both sides resolve, the check stops being paid.
	 */
	private void resolveEmptyIngredients(Level world) {
		if (siftableIngredient != Ingredient.EMPTY && meshIngredient != Ingredient.EMPTY)
			return;
		if (world == null)
			return;
		long now = world.getGameTime();
		if (now - lastEmptyResolveAttempt < 100)
			return;
		lastEmptyResolveAttempt = now;
		classifyIngredients();
		if (!warnedEmptyIngredients
				&& (siftableIngredient == Ingredient.EMPTY || meshIngredient == Ingredient.EMPTY)) {
			warnedEmptyIngredients = true;
			CreateSifter.LOGGER.warn("Sifting recipe {} has ingredients that resolve to no items and can never match",
					id);
		}
	}

	public Ingredient getSiftableIngredient(){
		return siftableIngredient;
	}

	public Ingredient getMeshIngredient(){
		return meshIngredient;
	}

	public ItemStack getMeshItemStack() {
		return meshStack;
	}

	public ItemStack getSiftableItemStack(){
		return siftableIngredientStack;
	}

	public static boolean isMeshItemStack(ItemStack itemStack){
		if(itemStack.getItem() instanceof BaseMesh || itemStack.getItem() instanceof AdvancedBaseMesh)
			return true;
		return false;
	}

	private static boolean isAdvancedMesh(ItemStack meshStack){
		return meshStack.getItem() instanceof AdvancedBaseMesh;
	}

	public boolean isWaterlogged() {
		return waterlogged;
	}

	public boolean hasSpeedRequirement(){
		return minimumSpeed > SifterConfig.SIFTER_MINIMUM_SPEED.get().floatValue();
	}

	public boolean requiresAdvancedMesh(){
		return advanced;
	}

	@Override
	public void readAdditional(JsonObject json) {
		super.readAdditional(json);
		waterlogged = GsonHelper.getAsBoolean(json, "waterlogged", false);
		minimumSpeed = GsonHelper.getAsFloat(json, "minimumSpeed", 1);
	}

	@Override
	public void writeAdditional(JsonObject json) {
		super.writeAdditional(json);
		if (waterlogged)
			json.addProperty("waterlogged", waterlogged);
		if(hasSpeedRequirement()){
			json.addProperty("minimumSpeed", minimumSpeed);
		}
	}

	@Override
	public void readAdditional(FriendlyByteBuf buffer) {
		super.readAdditional(buffer);
		waterlogged = buffer.readBoolean();
		minimumSpeed = buffer.readFloat();
	}

	@Override
	public void writeAdditional(FriendlyByteBuf buffer) {
		super.writeAdditional(buffer);
		buffer.writeBoolean(waterlogged);
		buffer.writeFloat(minimumSpeed);
	}
	public float getSpeedRequirement(){
		return this.minimumSpeed;
	}

	public List<ProcessingOutput> getRollableResults() {
		return results;
	}

	@Override
	public int getProcessingDuration() {
		float multiplier = Difficulty.effectiveTimeMultiplier();
		return Math.max(1, (int) (this.processingDuration * multiplier));
	}

	/** Base duration from the recipe file, without the difficulty time multiplier. */
	public int getBaseProcessingDuration() {
		return this.processingDuration;
	}

	@Override
	public List<ItemStack> rollResults(List<ProcessingOutput> rollableResults) {
		float chanceMultiplier = Math.max(0f, Difficulty.effectiveChanceMultiplier());
		RandomSource random = ROLL_RANDOM;
		boolean netheriteEnabled = SifterConfig.ENABLE_NETHERITE_SIFT.get();
		List<ItemStack> rolled = new ArrayList<>();
		for (int i = 0; i < rollableResults.size(); i++) {
			ProcessingOutput output = rollableResults.get(i);
			// Netherite drops only roll when the master switch is on; otherwise they never drop.
			if (!netheriteEnabled && isNetheriteFamily(output.getStack().getItem()))
				continue;
			float chance = Math.min(1f, output.getChance() * chanceMultiplier);
			int baseCount = output.getStack().getCount();

			// Per-recipe override (chance % and count) wins over the global multiplier.
			int[] override = SifterConfig.getOverride(this.id.toString(), i);
			int count = baseCount;
			if (override != null) {
				chance = Math.min(1f, override[0] / 100f);
				count = Math.min(output.getStack().getMaxStackSize(), Math.max(1, override[1]));
			}

			if (random.nextFloat() < chance) {
				ItemStack stack = output.getStack().copy();
				stack.setCount(count);
				rolled.add(stack);
			}
		}
		return rolled;
	}

	private static boolean isNetheriteFamily(Item item) {
		return item == Items.ANCIENT_DEBRIS || item == Items.NETHERITE_SCRAP || item == Items.NETHERITE_INGOT;
	}

	public List<ItemStack> rollResults() {
		return rollResults(this.getRollableResults());
	}

	public static boolean canHandSift(Level world, ItemStack stack, ItemStack mesh, boolean waterlogged) {
		return getMatchingInHandRecipes(world, stack, mesh, waterlogged, 0);
	}

	public static List<ItemStack> applyHandSift(Level world, Vec3 position, ItemStack stack, ItemStack mesh, boolean waterlogged) {
		ItemStackHandlerContainer tester = new ItemStackHandlerContainer(2);
		tester.setStackInSlot(0, stack);
		tester.setStackInSlot(1, mesh);
		Optional<SiftingRecipe> recipe = ModRecipeTypes.SIFTING.find(tester, world, waterlogged, 0, isAdvancedMeshItem(mesh));

		if (recipe.isPresent())
			return recipe.get().rollResults();
		return Collections.singletonList(stack);
	}

	public static boolean getMatchingInHandRecipes(Level world, ItemStack stack, ItemStack mesh, boolean waterlogged, float speed) {
		ItemStackHandlerContainer tester = new ItemStackHandlerContainer(2);
		tester.setStackInSlot(0, stack);
		tester.setStackInSlot(1, mesh);

		return ModRecipeTypes.SIFTING.find(tester, world, waterlogged, speed, isAdvancedMeshItem(mesh)).isPresent();
	}

	private static boolean isAdvancedMeshItem(ItemStack mesh) {
		return mesh.getItem() instanceof AdvancedBaseMesh;
	}

	public float getMinimumSpeed() {
		return minimumSpeed;
	}
}

