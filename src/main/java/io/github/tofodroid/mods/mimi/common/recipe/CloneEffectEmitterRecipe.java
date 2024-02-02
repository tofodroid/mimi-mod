package io.github.tofodroid.mods.mimi.common.recipe;

import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.TileEffectEmitter;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Arrays;

public class CloneEffectEmitterRecipe extends CustomRecipe {
    public static final String REGISTRY_NAME = "cloneeffectemitter";
    public static final List<Item> ITEMS = Arrays.asList(ModItems.EFFECTEMITTER);
	public static final SimpleCraftingRecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<CloneEffectEmitterRecipe>(CloneEffectEmitterRecipe::new);

    public CloneEffectEmitterRecipe(ResourceLocation recipeId, CraftingBookCategory category) {
        super(recipeId, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack source = ItemStack.EMPTY;
        Integer foundSlots = 1;
        List<Integer> validSlots = null;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);
            if (!stackI.isEmpty() && !isValid(stackI)) {
                // Invalid item found
                return false;
            } else if(!stackI.isEmpty() && source.isEmpty()) {
                validSlots = getValidSlots(i, inv.getWidth(), inv.getHeight());

                if(validSlots != null) { 
                    source = stackI;
                } else {
                    return false;
                }
            } else if(!source.isEmpty())  {
                if(validSlots.contains(i) && isValid(stackI)) {
                    foundSlots++;
                } else if(!stackI.isEmpty() && (!validSlots.contains(i) || !isValid(stackI))) {
                    return false;
                }
            }
        }

        return !source.isEmpty() && foundSlots == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess r) {
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
            TagUtils.setOrRemoveString(result, TileEffectEmitter.SOUND_ID_TAG, TagUtils.getStringOrDefault(source, TileEffectEmitter.SOUND_ID_TAG,  ""));
            TagUtils.setOrRemoveString(result, TileEffectEmitter.PARTICLE_ID_TAG, TagUtils.getStringOrDefault(source, TileEffectEmitter.PARTICLE_ID_TAG,  ""));
            TagUtils.setOrRemoveByte(result, TileEffectEmitter.VOLUME_TAG, TagUtils.getByteOrDefault(source, TileEffectEmitter.VOLUME_TAG,  5));
            TagUtils.setOrRemoveByte(result, TileEffectEmitter.PITCH_TAG, TagUtils.getByteOrDefault(source, TileEffectEmitter.PITCH_TAG,  0));
            TagUtils.setOrRemoveByte(result, TileEffectEmitter.SIDE_TAG, TagUtils.getByteOrDefault(source, TileEffectEmitter.SIDE_TAG,  0));
            TagUtils.setOrRemoveByte(result, TileEffectEmitter.SPREAD_TAG, TagUtils.getByteOrDefault(source, TileEffectEmitter.SPREAD_TAG,  0));
            TagUtils.setOrRemoveByte(result, TileEffectEmitter.COUNT_TAG, TagUtils.getByteOrDefault(source, TileEffectEmitter.COUNT_TAG,  1));
            TagUtils.setOrRemoveByte(result, TileEffectEmitter.SPEED_X_TAG, TagUtils.getByteOrDefault(source, TileEffectEmitter.SPEED_X_TAG,  0));
            TagUtils.setOrRemoveByte(result, TileEffectEmitter.SPEED_Y_TAG, TagUtils.getByteOrDefault(source, TileEffectEmitter.SPEED_Y_TAG,  0));
            TagUtils.setOrRemoveByte(result, TileEffectEmitter.SPEED_Z_TAG, TagUtils.getByteOrDefault(source, TileEffectEmitter.SPEED_Z_TAG,  0));
            TagUtils.setOrRemoveInt(result, TileEffectEmitter.SOUND_LOOP_TAG, TagUtils.getIntOrDefault(source, TileEffectEmitter.SOUND_LOOP_TAG,  0));
            TagUtils.setOrRemoveInt(result, TileEffectEmitter.PARTICLE_LOOP_TAG, TagUtils.getIntOrDefault(source, TileEffectEmitter.PARTICLE_LOOP_TAG,  0));
            TagUtils.setOrRemoveBoolean(result, TileEffectEmitter.INVERTED_TAG, TagUtils.getBooleanOrDefault(source, TileEffectEmitter.INVERTED_TAG,  false));
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            if(isValid(inv.getItem(i))) {
                nonnulllist.set(i, inv.getItem(i).copyWithCount(1));
                break;
            }
        }

        return nonnulllist;
   }

    protected Boolean isValid(ItemStack stack) {
        return ITEMS.contains(stack.getItem());
    }

    protected List<Integer> getValidSlots(Integer slot, Integer width, Integer height) {
        // Validate
        Integer slotCol = slot % height;

        if(slotCol < (width-1)) {
            return Arrays.asList(slot, slot + 1);
        }

        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CloneEffectEmitterRecipe.SERIALIZER;
    }
}