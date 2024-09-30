package io.github.tofodroid.mods.mimi.common.recipe;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.tofodroid.mods.mimi.common.block.AColoredBlock;
import io.github.tofodroid.mods.mimi.common.block.BlockLedCube;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class ChangeLedCubeRecipe extends ShapedRecipe {
	public static final ChangeLedCubeRecipe.ChangeLedSerializer SERIALIZER = new ChangeLedCubeRecipe.ChangeLedSerializer();

    final ShapedRecipePattern shapedPattern;

    public ChangeLedCubeRecipe(ShapedRecipe shaped) {
        super(shaped.getGroup(), shaped.category(), new ShapedRecipePattern(shaped.getWidth(), shaped.getHeight(), shaped.getIngredients(), Optional.empty()), shaped.getResultItem(null), shaped.showNotification());
        shapedPattern = new ShapedRecipePattern(shaped.getWidth(), shaped.getHeight(), shaped.getIngredients(), Optional.empty());
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
    public ItemStack assemble(CraftingInput pInput, HolderLookup.Provider pRegistries) {
        ItemStack source = ItemStack.EMPTY;
        Integer sourceDye = null;

        // Ensure dye colors match for all source cubes
        for (int i = 0; i < pInput.size(); i++) {
            ItemStack stackI = pInput.getItem(i);

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
        ItemStack result = this.getResultItem(null).copy();
        result.applyComponents(source.getComponents());
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static class ChangeLedSerializer implements RecipeSerializer<ChangeLedCubeRecipe> {
        public static final String REGISTRY_NAME = "changeledcube";
    
        public static final StreamCodec<RegistryFriendlyByteBuf, ChangeLedCubeRecipe> STREAM_CODEC = StreamCodec.of(
            ChangeLedCubeRecipe.ChangeLedSerializer::toNetwork, ChangeLedCubeRecipe.ChangeLedSerializer::fromNetwork
        );

        @Override
        public MapCodec<ChangeLedCubeRecipe> codec() {
            return RecordCodecBuilder.mapCodec(
                p_327208_ -> p_327208_.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(p_309251_ -> p_309251_.getGroup()),
                    CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_309253_ -> p_309253_.category()),
                    ShapedRecipePattern.MAP_CODEC.forGetter(p_309254_ -> p_309254_.shapedPattern),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_309252_ -> p_309252_.getResultItem(null)),
                    Codec.BOOL.optionalFieldOf("show_notification", Boolean.valueOf(true)).forGetter(p_309255_ -> p_309255_.showNotification())
                )
                .apply(p_327208_, ChangeLedCubeRecipe::new)
            );
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChangeLedCubeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ChangeLedCubeRecipe fromNetwork(RegistryFriendlyByteBuf p_335571_) {
            return new ChangeLedCubeRecipe(ShapedRecipe.Serializer.STREAM_CODEC.decode(p_335571_));
        }

        private static void toNetwork(RegistryFriendlyByteBuf p_336365_, ShapedRecipe p_330934_) {
            ShapedRecipe.Serializer.STREAM_CODEC.encode(p_336365_, p_330934_);
        }
    }
}