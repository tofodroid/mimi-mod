package io.github.tofodroid.mods.mimi.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
        
        @SuppressWarnings("deprecation")
        public static final Codec<ItemStack> ITEM_STACK_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemStack::getItem)
            ).apply(instance, ItemStack::new)
        );
        
        @Override
        public Codec<TuningTableRecipe> codec() {
            return RecordCodecBuilder.create(instance -> instance.group(
						Ingredient.CODEC.fieldOf("instrument").forGetter(o -> o.instrument),
						Ingredient.CODEC.fieldOf("addition").forGetter(o -> o.addition),
						ITEM_STACK_ITEM_CODEC.fieldOf("result").forGetter(o -> o.result)
            ).apply(instance, TuningTableRecipe::new));
        }

        @Override
        public TuningTableRecipe fromNetwork(FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            Ingredient ingredient1 = Ingredient.fromNetwork(buffer);
            ItemStack itemstack = buffer.readItem();
            return new TuningTableRecipe(ingredient, ingredient1, itemstack);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TuningTableRecipe recipe) {
            recipe.instrument.toNetwork(buffer);
            recipe.addition.toNetwork(buffer);
            buffer.writeItem(recipe.result);
        }
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess r) {
        ItemStack itemstack = this.result.copy();
        CompoundTag compoundnbt = inv.getItem(0).getTag();
        if (compoundnbt != null) {
            itemstack.setTag(compoundnbt.copy());
        }

        return itemstack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess r) {
        return this.result;
    }
}
