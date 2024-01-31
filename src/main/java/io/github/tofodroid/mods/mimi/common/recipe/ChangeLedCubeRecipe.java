package io.github.tofodroid.mods.mimi.common.recipe;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import io.github.tofodroid.mods.mimi.common.block.AColoredBlock;
import io.github.tofodroid.mods.mimi.common.block.BlockLedCube;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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
        super(shaped.getId(), shaped.getGroup(), shaped.category(), shaped.getWidth(), shaped.getHeight(), shaped.getIngredients(), shaped.getResultItem(null), shaped.showNotification());
    }

    public ChangeLedCubeRecipe(ResourceLocation p_250963_, String p_250221_, CraftingBookCategory p_250716_, int p_251480_, int p_251980_, NonNullList<Ingredient> p_252150_, ItemStack p_248581_) {
        super(p_250963_, p_250221_, p_250716_, p_251480_, p_251980_, p_252150_, p_248581_);
    }   
    
    public ChangeLedCubeRecipe(ResourceLocation p_273203_, String p_272759_, CraftingBookCategory p_273506_, int p_272952_, int p_272920_, NonNullList<Ingredient> p_273650_, ItemStack p_272852_, boolean p_273122_) {
        super(p_273203_, p_272759_, p_273506_, p_272952_, p_272920_, p_273650_, p_272852_, p_273122_);
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
        return CloneMidiSettingsRecipe.SERIALIZER;
    }

    public static class ChangeLedSerializer implements RecipeSerializer<ChangeLedCubeRecipe>{
        public static final String REGISTRY_NAME = "changeledcube";

        private Serializer sueprSerializer = new Serializer();

        @Override
        public ChangeLedCubeRecipe fromJson(ResourceLocation p_44103_, JsonObject p_44104_) {
            return new ChangeLedCubeRecipe(sueprSerializer.fromJson(p_44103_, p_44104_));
        }

        @Override
        public @Nullable ChangeLedCubeRecipe fromNetwork(ResourceLocation p_44105_, FriendlyByteBuf p_44106_) {
            return new ChangeLedCubeRecipe(sueprSerializer.fromNetwork(p_44105_, p_44106_));
        }

        @Override
        public void toNetwork(FriendlyByteBuf p_44101_, ChangeLedCubeRecipe p_44102_) {
            sueprSerializer.toNetwork(p_44101_, p_44102_);
        }
    }
}