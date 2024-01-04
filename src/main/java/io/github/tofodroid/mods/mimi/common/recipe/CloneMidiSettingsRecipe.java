package io.github.tofodroid.mods.mimi.common.recipe;

import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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

public class CloneMidiSettingsRecipe extends CustomRecipe {
    public static final String REGISTRY_NAME = "clonemidi";
    public static final List<Item> MIDI_ITEMS = Arrays.asList(ModItems.CONDUCTOR, ModItems.LISTENER, ModItems.RECEIVER);
    public static final List<String> COPY_TAGS = Arrays.asList(
        InstrumentDataUtils.FILTER_NOTE_TAG, InstrumentDataUtils.FILTER_OCT_TAG, InstrumentDataUtils.INVERT_NOTE_OCT_TAG, 
        InstrumentDataUtils.BROADCAST_NOTE_TAG, InstrumentDataUtils.SOURCE_TAG, InstrumentDataUtils.SOURCE_NAME_TAG, 
        InstrumentDataUtils.SYS_INPUT_TAG, InstrumentDataUtils.ENABLED_CHANNELS_TAG, InstrumentDataUtils.INSTRUMENT_TAG,
        InstrumentDataUtils.INVERT_INSTRUMENT_TAG, InstrumentDataUtils.VOLUME_TAG
    );

	public static final SimpleCraftingRecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<CloneMidiSettingsRecipe>(CloneMidiSettingsRecipe::new);

    public CloneMidiSettingsRecipe(ResourceLocation recipeId, CraftingBookCategory category) {
        super(recipeId, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack source = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);
            if (!stackI.isEmpty() && !canStoreMidiSettings(stackI)) {
                // Invalid item found
                return false;
            } else if (source.isEmpty() && canStoreMidiSettings(stackI)) {
                source = stackI;
            } else if (target.isEmpty() && canCopyFromSourceToTarget(source, stackI))  {
                target = stackI;
            } else if(!stackI.isEmpty() && !source.isEmpty() && !target.isEmpty()) {
                // Too many valid items found
                return false;
            }
        }

        return !source.isEmpty() && !target.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess r) {
        ItemStack source = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);
            if (source.isEmpty() && canStoreMidiSettings(stackI)) {
                source = stackI;
            } else if (target.isEmpty() && canCopyFromSourceToTarget(source, stackI))  {
                target = stackI;
            }
        }
        
        if(!source.isEmpty() && !target.isEmpty()) {
            CompoundTag sourceTag = source.getOrCreateTag();
            CompoundTag targetTag = target.getOrCreateTag().copy();

            for(String toCopy : COPY_TAGS) {
                if(sourceTag.contains(toCopy)) {
                    targetTag.put(toCopy, sourceTag.get(toCopy));
                } else {
                    targetTag.remove(toCopy);
                }
            }

            ItemStack result = target.copyWithCount(1);
            result.setTag(targetTag);
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            if(canStoreMidiSettings(inv.getItem(i))) {
                nonnulllist.set(i, inv.getItem(i).copyWithCount(inv.getItem(i).getCount()));
                break;
            }
        }

        return nonnulllist;
   }

    protected Boolean canStoreMidiSettings(ItemStack stack) {
        return stack.getItem() instanceof IInstrumentItem ||
            MIDI_ITEMS.contains(stack.getItem());
    }

    protected Boolean canCopyFromSourceToTarget(ItemStack source, ItemStack target) {
        return (source.getItem() instanceof IInstrumentItem && target.getItem() instanceof IInstrumentItem) ||
            source.getItem().equals(target.getItem());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CloneMidiSettingsRecipe.SERIALIZER;
    }
}