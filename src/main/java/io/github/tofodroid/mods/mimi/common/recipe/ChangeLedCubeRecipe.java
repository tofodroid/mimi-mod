package io.github.tofodroid.mods.mimi.common.recipe;

import io.github.tofodroid.mods.mimi.common.block.AColoredBlock;
import io.github.tofodroid.mods.mimi.common.block.BlockLedCube;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ChangeLedCubeRecipe extends ShapedRecipe {
	public static final ChangeLedCubeRecipe.ChangeLedSerializer SERIALIZER = new ChangeLedCubeRecipe.ChangeLedSerializer();

    public ChangeLedCubeRecipe(ShapedRecipe shaped) {
        super(shaped.getGroup(), shaped.category(), shaped.getWidth(), shaped.getHeight(), shaped.getIngredients(), shaped.getResultItem(null), shaped.showNotification());
    }

    public ChangeLedCubeRecipe(String p_250221_, CraftingBookCategory p_250716_, int p_251480_, int p_251980_, NonNullList<Ingredient> p_252150_, ItemStack p_248581_) {
        super(p_250221_, p_250716_, p_251480_, p_251980_, p_252150_, p_248581_);
    }   
    
    public ChangeLedCubeRecipe(String p_272759_, CraftingBookCategory p_273506_, int p_272952_, int p_272920_, NonNullList<Ingredient> p_273650_, ItemStack p_272852_, boolean p_273122_) {
        super(p_272759_, p_273506_, p_272952_, p_272920_, p_273650_, p_272852_, p_273122_);
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess reg) {
        ItemStack source = ItemStack.EMPTY;
        Integer sourceDye = null;

        // Ensure dye colors match for all source cubes
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);

            if(!stackI.isEmpty()) {
                if(stackI.getItem() instanceof BlockItem && ((BlockItem)stackI.getItem()).getBlock() instanceof BlockLedCube) {
                    Integer dyeColor = TagUtils.getIntOrDefault(stackI, AColoredBlock.DYE_ID.getName(), 0);

                    if(source.isEmpty()) {
                        source = stackI;
                        sourceDye = dyeColor;
                    } else if(sourceDye != dyeColor) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        
        // At this point, valid
        ItemStack result = this.getResultItem(reg).copy();
        result.setTag(source.getOrCreateTag());
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class ChangeLedSerializer extends GenericShapedRecipeSerializer<ChangeLedCubeRecipe> {
        public static final String REGISTRY_NAME = "changeledcube";
    
        @Override
        public ChangeLedCubeRecipe fromShapedRecipe(ShapedRecipe decoded) {
            return new ChangeLedCubeRecipe(decoded);
        }
    }
}