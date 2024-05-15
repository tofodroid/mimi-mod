package io.github.tofodroid.mods.mimi.forge.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class GenericAddDropLootModifier extends LootModifier {
    public static final MapCodec<GenericAddDropLootModifier> CODEC = RecordCodecBuilder.mapCodec(
        inst -> LootModifier.codecStart(inst)
        .and(
                ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item)
        )
        .apply(inst, GenericAddDropLootModifier::new)
    );

    public Item item;

    protected GenericAddDropLootModifier(LootItemCondition[] conditionsIn, Item item) {
        super(conditionsIn);
        this.item = item;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(new ItemStack(item, 1));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}

