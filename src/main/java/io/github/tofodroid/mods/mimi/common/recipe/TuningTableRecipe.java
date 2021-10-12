package io.github.tofodroid.mods.mimi.common.recipe;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import com.google.gson.JsonObject;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TuningTableRecipe implements IRecipe<IInventory> {
	public static IRecipeType<TuningTableRecipe> TYPE = IRecipeType.register(MIMIMod.MODID + ":tuning");
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

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    public boolean matches(IInventory inv, World worldIn) {
        return this.instrument.test(inv.getStackInSlot(0)) && this.addition.test(inv.getStackInSlot(1));
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    public ItemStack getCraftingResult(IInventory inv) {
        ItemStack itemstack = this.result.copy();
        CompoundNBT compoundnbt = inv.getStackInSlot(0).getTag();
        if (compoundnbt != null) {
            itemstack.setTag(compoundnbt.copy());
        }

        return itemstack;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    /**
     * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
     * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
     */
    public ItemStack getRecipeOutput() {
        return this.result;
    }

    public boolean isValidAdditionItem(ItemStack addition) {
        return this.addition.test(addition);
    }

    public ItemStack getIcon() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    public ResourceLocation getId() {
        return this.recipeId;
    }

    public IRecipeSerializer<?> getSerializer() {
        return TuningTableRecipe.SERIALIZER;
    }

    public IRecipeType<?> getType() {
        return TuningTableRecipe.TYPE;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(IInventory inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
  
        for(int i = 0; i < nonnulllist.size(); ++i) {
           ItemStack item = inv.getStackInSlot(i);
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

    public static class Serializer extends net.minecraftforge.registries.ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<TuningTableRecipe> {
        Serializer() {
			this.setRegistryName(new ResourceLocation(MIMIMod.MODID, "tuning"));
		}
        
        public TuningTableRecipe read(ResourceLocation recipeId, JsonObject json) {
            Ingredient ingredient = Ingredient.deserialize(JSONUtils.getJsonObject(json, "instrument"));
            Ingredient ingredient1 = Ingredient.deserialize(JSONUtils.getJsonObject(json, "addition"));
            ItemStack itemstack = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result"));
            return new TuningTableRecipe(recipeId, ingredient, ingredient1, itemstack);
        }

        public TuningTableRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            Ingredient ingredient = Ingredient.read(buffer);
            Ingredient ingredient1 = Ingredient.read(buffer);
            ItemStack itemstack = buffer.readItemStack();
            return new TuningTableRecipe(recipeId, ingredient, ingredient1, itemstack);
        }

        public void write(PacketBuffer buffer, TuningTableRecipe recipe) {
            recipe.instrument.write(buffer);
            recipe.addition.write(buffer);
            buffer.writeItemStack(recipe.result);
        }
    }
}
