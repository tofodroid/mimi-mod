package io.github.tofodroid.mods.mimi.common.recipe;

import java.util.Arrays;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.block.AColoredBlock;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class CopyBlockDyeRecipe extends CustomRecipe {
    public static final String REGISTRY_NAME = "copyblockdye";

	public static final SimpleCraftingRecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<CopyBlockDyeRecipe>(CopyBlockDyeRecipe::new);

    public CopyBlockDyeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack source = ItemStack.EMPTY;
        Integer foundSlots = 1;
        List<Integer> validSlots = null;

        for(int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);
            if(!stackI.isEmpty() && !isAllowedItem(stackI)) {
                // Invalid item found
                return false;
            } else if(!stackI.isEmpty() && source.isEmpty()) {
                validSlots = getValidSlots(i, inv.getWidth(), inv.getHeight());

                if(validSlots != null) { 
                    source = stackI;
                } else {
                    return false;
                }
            } else if(!source.isEmpty()) {
                if(validSlots.contains(i) && isAllowedItem(stackI)) {
                    foundSlots++;
                } else if(!validSlots.contains(i) && !stackI.isEmpty()) {
                    return false;
                }
            }
        }

        return !source.isEmpty() && foundSlots == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, HolderLookup.Provider pRegistries) {
        ItemStack source = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);

            if(!stackI.isEmpty() && source.isEmpty()) {
                source = stackI;
            } else if(!stackI.isEmpty() && !source.isEmpty()) {
                target = stackI;
            }
        }
        
        if(!source.isEmpty() && !target.isEmpty()) {
            ItemStack result = target.copyWithCount(1);
            DataComponentType<Integer> DYE_COMPONENT = TagUtils.getIntComponent(AColoredBlock.DYE_ID.getName());
            result.set(DYE_COMPONENT, TagUtils.getOrDefault(source, DYE_COMPONENT, 0));
            return result;
        }

        return ItemStack.EMPTY;
    }


    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            if(isAllowedItem(inv.getItem(i))) {
                nonnulllist.set(i, inv.getItem(i).copyWithCount(1));
                break;
            }
        }

        return nonnulllist;
   }

    protected List<Integer> getValidSlots(Integer slot, Integer width, Integer height) {
        // Validate
        Integer slotCol = slot % height;

        if(slotCol < (width-1)) {
            return Arrays.asList(slot, slot + 1);
        }

        return null;
    }

    protected Boolean isAllowedItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem &&
            ((BlockItem)stack.getItem()).getBlock() instanceof AColoredBlock;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}