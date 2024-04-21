package io.github.tofodroid.mods.mimi.neoforge.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class GenericAddDropLootModifier extends LootModifier {
    public static final Codec<GenericAddDropLootModifier> CODEC = RecordCodecBuilder.create(
        inst -> LootModifier.codecStart(inst)
        .and(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item)
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
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.GENERIC_ADD_DROP.get();
    }
}

