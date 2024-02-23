package io.github.tofodroid.mods.mimi.common.recipe;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.tofodroid.mods.mimi.common.block.AColoredBlock;
import io.github.tofodroid.mods.mimi.common.block.BlockLedCube;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class ChangeLedCubeRecipe extends ShapedRecipe {
	public static final ChangeLedCubeRecipe.ChangeLedSerializer SERIALIZER = new ChangeLedCubeRecipe.ChangeLedSerializer();

    final ShapedRecipePattern shapedPattern;

    public ChangeLedCubeRecipe(ShapedRecipe shaped) {
        super(shaped.getGroup(), shaped.category(), new ShapedRecipePattern(shaped.getRecipeWidth(), shaped.getRecipeHeight(), shaped.getIngredients(), Optional.empty()), shaped.getResultItem(null), shaped.showNotification());
        shapedPattern = new ShapedRecipePattern(shaped.getRecipeWidth(), shaped.getRecipeHeight(), shaped.getIngredients(), Optional.empty());
    }

    public ChangeLedCubeRecipe(String p_250221_, CraftingBookCategory p_250716_, ShapedRecipePattern p_312200_, ItemStack p_248581_, boolean p_310619_) {
        super(p_250221_, p_250716_, p_312200_, p_248581_, p_310619_);
        shapedPattern = p_312200_;
    }   
    
    public ChangeLedCubeRecipe(String p_272759_, CraftingBookCategory p_273506_, ShapedRecipePattern p_310709_, ItemStack p_272852_) {
        super(p_272759_, p_273506_, p_310709_, p_272852_);
        shapedPattern = p_310709_;
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

    public static class ChangeLedSerializer implements RecipeSerializer<ChangeLedCubeRecipe> {
        public static final String REGISTRY_NAME = "changeledcube";
    
        @Override
        public Codec<ChangeLedCubeRecipe> codec() {
            return RecordCodecBuilder.create((p_309256_) -> {
                return p_309256_.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((p_309251_) -> {
                    return p_309251_.getGroup();
                }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((p_309253_) -> {
                    return p_309253_.category();
                }), ShapedRecipePattern.MAP_CODEC.forGetter((p_309254_) -> {
                    return p_309254_.shapedPattern;
                }), ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter((p_309252_) -> {
                    return p_309252_.getResultItem(null);
                }), ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter((p_309255_) -> {
                    return p_309255_.showNotification();
                })).apply(p_309256_, ChangeLedCubeRecipe::new);
            });
        }

        @Override
        public @Nullable ChangeLedCubeRecipe fromNetwork(FriendlyByteBuf p_44106_) {
            return new ChangeLedCubeRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(p_44106_));
        }

        @Override
        public void toNetwork(FriendlyByteBuf p_44101_, ChangeLedCubeRecipe p_44102_) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(p_44101_, p_44102_);
        }
    }
}