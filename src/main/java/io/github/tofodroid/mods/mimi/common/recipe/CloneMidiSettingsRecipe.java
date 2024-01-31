package io.github.tofodroid.mods.mimi.common.recipe;

import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
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

public class CloneMidiSettingsRecipe extends CustomRecipe {
    public static final String REGISTRY_NAME = "clonemidi";
    public static final List<Item> MIDI_ITEMS = Arrays.asList(ModItems.CONDUCTOR, ModItems.LISTENER, ModItems.RECEIVER);
	public static final SimpleCraftingRecipeSerializer<?> SERIALIZER = new SimpleCraftingRecipeSerializer<CloneMidiSettingsRecipe>(CloneMidiSettingsRecipe::new);

    public CloneMidiSettingsRecipe(ResourceLocation recipeId, CraftingBookCategory category) {
        super(recipeId, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        ItemStack source = ItemStack.EMPTY;
        Integer foundSlots = 1;
        List<Integer> validSlots = null;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stackI = inv.getItem(i);
            if (!stackI.isEmpty() && !canStoreMidiSettings(stackI)) {
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
                if(validSlots.contains(i) && canCopyFromSourceToTarget(source, stackI)) {
                    foundSlots++;
                } else if(!stackI.isEmpty() && (!validSlots.contains(i) || !canCopyFromSourceToTarget(source, stackI))) {
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
            InstrumentDataUtils.setMidiSource(result, InstrumentDataUtils.getMidiSource(source), InstrumentDataUtils.getMidiSourceName(source, false));
            InstrumentDataUtils.setEnabledChannelsInt(result, InstrumentDataUtils.getEnabledChannelsInt(source));

            if(source.getItem() instanceof IInstrumentItem) {
                InstrumentDataUtils.setSysInput(result, InstrumentDataUtils.getSysInput(source));
                InstrumentDataUtils.setInstrumentVolume(result, InstrumentDataUtils.getInstrumentVolume(source));
            } else {
                InstrumentDataUtils.setFilterOct(result, InstrumentDataUtils.getFilterOct(source));
                InstrumentDataUtils.setFilterNote(result, InstrumentDataUtils.getFilterNote(source));
                InstrumentDataUtils.setInvertNoteOct(result, InstrumentDataUtils.getInvertNoteOct(source));
                InstrumentDataUtils.setFilterInstrument(result, InstrumentDataUtils.getFilterInstrument(source));
                InstrumentDataUtils.setInvertInstrument(result, InstrumentDataUtils.getInvertInstrument(source));
                InstrumentDataUtils.setInvertSignal(result, InstrumentDataUtils.getInvertSignal(source));
            }
            return result;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);

        for(int i = 0; i < nonnulllist.size(); ++i) {
            if(canStoreMidiSettings(inv.getItem(i))) {
                nonnulllist.set(i, inv.getItem(i).copyWithCount(1));
                break;
            }
        }

        return nonnulllist;
   }

    protected Boolean canStoreMidiSettings(ItemStack stack) {
        return stack.getItem() instanceof IInstrumentItem ||
            MIDI_ITEMS.contains(stack.getItem());
    }

    protected List<Integer> getValidSlots(Integer slot, Integer width, Integer height) {
        // Validate
        Integer slotCol = slot % height;

        if(slotCol < (width-1)) {
            return Arrays.asList(slot, slot + 1);
        }

        return null;
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