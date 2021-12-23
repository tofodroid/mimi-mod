package io.github.tofodroid.mods.mimi.common.recipe;

import com.google.gson.JsonObject;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class TuningTableRecipe implements Recipe<Inventory> {
	public static RecipeType<TuningTableRecipe> TYPE = RecipeType.register(MIMIMod.MODID + ":tuning");
	public static final TuningTableRecipe.Serializer SERIALIZER = new TuningTableRecipe.Serializer();

    private final Ingredient instrument;
    private final Ingredient addition;
    private final ItemStack result;
    private final ResourceLocation recipeId;

    public TuningTableRecipe(ResourceLocation recipeId, Ingredient instrument, Ingredient addition, ItemStack result) {
        this.recipeId = recipeId;
        this.instrument = instrument;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public boolean matches(Inventory inv, Level worldIn) {
        return this.instrument.test(inv.getItem(0)) && this.addition.test(inv.getItem(1));
    }

    // JEI
    public ItemStack getIcon() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    @Override
    public ResourceLocation getId() {
        return this.recipeId;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return TuningTableRecipe.SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return TuningTableRecipe.TYPE;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(Inventory inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
  
        for(int i = 0; i < nonnulllist.size(); ++i) {
           ItemStack item = inv.getItem(i);
           if (item.hasContainerItem()) {
              nonnulllist.set(i, item.getContainerItem());
           } else if(item.getCount() > 1) {
                ItemStack remaining = item.copy();
                remaining.setCount(item.getCount()-1);
                nonnulllist.set(i, remaining);
           }
        }
  
        return nonnulllist;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.add(0,instrument);
        result.add(1,addition);
        return result;
    }

    public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<TuningTableRecipe> {
        Serializer() {
			this.setRegistryName(new ResourceLocation(MIMIMod.MODID, "tuning"));
		}
        
        @Override
        public TuningTableRecipe fromJson(ResourceLocation resource, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(json.get("instrument"));
            Ingredient ingredient1 = Ingredient.fromJson(json.get("addition"));
            ItemStack itemstack = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
            return new TuningTableRecipe(resource, ingredient, ingredient1, itemstack);
        }

        @Override
        public TuningTableRecipe fromNetwork(ResourceLocation resource, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            Ingredient ingredient1 = Ingredient.fromNetwork(buffer);
            ItemStack itemstack = buffer.readItem();
            return new TuningTableRecipe(resource, ingredient, ingredient1, itemstack);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TuningTableRecipe recipe) {
            recipe.instrument.toNetwork(buffer);
            recipe.addition.toNetwork(buffer);
            buffer.writeItem(recipe.result);
        }
    }

    @Override
    public ItemStack assemble(Inventory inv) {
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
    public ItemStack getResultItem() {
        return this.result;
    }
}
