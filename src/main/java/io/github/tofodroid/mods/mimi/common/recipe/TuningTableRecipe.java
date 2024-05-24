package io.github.tofodroid.mods.mimi.common.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class TuningTableRecipe implements Recipe<CraftingContainer>{
	public static final TuningTableRecipe.Serializer SERIALIZER = new TuningTableRecipe.Serializer();

    private final Ingredient instrument;
    private final Ingredient addition;
    private final ItemStack result;

    public TuningTableRecipe(Ingredient instrument, Ingredient addition, ItemStack result) {
        this.instrument = instrument;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return this.instrument.test(inv.getItem(0)) && this.addition.test(inv.getItem(1));
    }

    // JEI
    public ItemStack getIcon() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    @Override
    public String getGroup() {
        return "";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TuningTableRecipe.SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.TUNING_TYPE;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.add(0,instrument);
        result.add(1,addition);
        return result;
    }

    public static class Serializer implements RecipeSerializer<TuningTableRecipe> {
        public static final String REGISTRY_NAME = "tuning";

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TuningTableRecipe> streamCodec() {
            return StreamCodec.of(
                TuningTableRecipe.Serializer::toNetwork, TuningTableRecipe.Serializer::fromNetwork
            );
        }
        
        @Override
        public MapCodec<TuningTableRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
						Ingredient.CODEC.fieldOf("instrument").forGetter(o -> o.instrument),
						Ingredient.CODEC.fieldOf("addition").forGetter(o -> o.addition),
						ItemStack.CODEC.fieldOf("result").forGetter(o -> o.result)
            ).apply(instance, TuningTableRecipe::new));
        }

        public static TuningTableRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient ingredient1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            return new TuningTableRecipe(ingredient, ingredient1, itemstack);
        }
        
        public static void toNetwork(RegistryFriendlyByteBuf buffer, TuningTableRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.instrument);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.addition);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider pRegistries) {
        ItemStack itemstack = this.result.copy();
        itemstack.applyComponents(inv.getItem(0).getComponents());
        return itemstack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider r) {
        return this.result;
    }
}
