package io.github.tofodroid.mods.mimi.common.recipe;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public abstract class GenericShapedRecipeSerializer<S extends ShapedRecipe> implements RecipeSerializer<S> {
    private RecipeSerializer<ShapedRecipe> superSerializer = RecipeSerializer.SHAPED_RECIPE;

    public abstract S fromShapedRecipe(ShapedRecipe decoded);

    @Override
    public Codec<S> codec() {
        return new Codec<S>() {
            @Override
            public <T> DataResult<T> encode(S input, DynamicOps<T> ops, T prefix) {
                return superSerializer.codec().encode(input, ops, prefix);
            }

            @Override
            public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<Pair<ShapedRecipe, T>> superResult = superSerializer.codec().decode(ops, input);
                
                if(superResult.error().isPresent()) {
                    Pair<ShapedRecipe, T> partialResult = superResult.resultOrPartial(s -> {}).get();
                    return DataResult.error(
                        superResult.error().get()::message,
                        new Pair<>(
                        fromShapedRecipe(partialResult.getFirst()),
                        partialResult.getSecond()
                    ));
                } else {
                    return DataResult.success(new Pair<>(
                        fromShapedRecipe(superResult.get().left().get().getFirst()), 
                        superResult.get().left().get().getSecond()
                    ));
                }
            }
        };
    }

    @Override
    public @Nullable S fromNetwork(FriendlyByteBuf p_44106_) {
        return fromShapedRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(p_44106_));
    }

    @Override
    public void toNetwork(FriendlyByteBuf p_44101_, S p_44102_) {
        RecipeSerializer.SHAPED_RECIPE.toNetwork(p_44101_, p_44102_);
    }
}
