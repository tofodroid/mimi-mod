package io.github.tofodroid.mods.mimi.common.recipe;

import java.util.Arrays;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.block.AColoredBlock;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class ChangeLedCubeRecipe extends CustomRecipe {
    public static final String REGISTRY_NAME = "changeledcube";

	public static final SimpleCraftingRecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<ChangeLedCubeRecipe>(ChangeLedCubeRecipe::new);

    public ChangeLedCubeRecipe(ResourceLocation recipeId, CraftingBookCategory category) {
        super(recipeId, CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack source = ItemStack.EMPTY;
        Integer foundSlots = 1;
        List<Integer> validSlots = null;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);
            if(!stackI.isEmpty() && !isAllowedItem(stackI)) {
                // Invalid item found
                return false;
            } else if(!stackI.isEmpty() & source.isEmpty()) {
                validSlots = getValidSlots(i, inv.getWidth(), inv.getHeight());

                if(validSlots != null) { 
                    source = stackI;
                } else {
                    return false;
                }
            } else if(!source.isEmpty()) {
                if(validSlots.contains(i) && ItemStack.isSameItemSameTags(stackI, source)) {
                    foundSlots++;
                } else if(!validSlots.contains(i) && !stackI.isEmpty()) {
                    return false;
                }
            }
        }

        return !source.isEmpty() && foundSlots == 4;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess r) {
        ItemStack source = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);
            if (!stackI.isEmpty() && source.isEmpty()) {
                source = stackI;
                break;
            }
        }

        Item newCube = getNextCube(source.getItem());
        
        if(newCube != null) {
            ItemStack stack = new ItemStack(newCube, 4);
            stack.setTag(source.getOrCreateTag().copy());
            return stack;
        }

        return ItemStack.EMPTY;
    }

    protected Item getNextCube(Item currentCube) {
        if(ModItems.LEDCUBE_A.equals(currentCube)) {
            return ModItems.LEDCUBE_B;
        } else if(ModItems.LEDCUBE_B.equals(currentCube)) {
            return ModItems.LEDCUBE_C;
        } else if(ModItems.LEDCUBE_C.equals(currentCube)) {
            return ModItems.LEDCUBE_D;
        } else if(ModItems.LEDCUBE_D.equals(currentCube)) {
            return ModItems.LEDCUBE_E;
        } else if(ModItems.LEDCUBE_E.equals(currentCube)) {
            return ModItems.LEDCUBE_F;
        } else if(ModItems.LEDCUBE_F.equals(currentCube)) {
            return ModItems.LEDCUBE_G;
        } else if(ModItems.LEDCUBE_G.equals(currentCube)) {
            return ModItems.LEDCUBE_H;
        } else if(ModItems.LEDCUBE_H.equals(currentCube)) {
            return ModItems.LEDCUBE_A;
        }
        return null;
    }

    protected List<Integer> getValidSlots(Integer slot, Integer width, Integer height) {
        // Validate
        Integer slotRow = slot / width;
        Integer slotCol = slot % height;

        if(slotCol < (width-1) && slotRow < (height-1)) {
            return Arrays.asList(slot, slot + 1, slot + width, slot + width + 1);
        }

        return null;
    }

    protected Boolean isAllowedItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem &&
            ((BlockItem)stack.getItem()).getBlock() instanceof AColoredBlock;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CloneMidiSettingsRecipe.SERIALIZER;
    }
}