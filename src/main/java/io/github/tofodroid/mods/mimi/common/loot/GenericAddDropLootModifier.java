package io.github.tofodroid.mods.mimi.common.loot;

import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraft.util.JSONUtils;

import java.util.List;

public class GenericAddDropLootModifier extends LootModifier {

    private final ItemStack stack;

    protected GenericAddDropLootModifier(ILootCondition[] conditionsIn, ItemStack itemStack) {
        super(conditionsIn);
        this.stack = itemStack;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(stack.copy());
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<GenericAddDropLootModifier> {
        @Override
        public GenericAddDropLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
            Item toAdd = JSONUtils.getItem(object, "item");
            return new GenericAddDropLootModifier(conditions, new ItemStack(toAdd,1));
        }

        @Override
        public JsonObject write(GenericAddDropLootModifier instance) {
            JsonObject jsonObject = makeConditions(instance.conditions);
            jsonObject.addProperty("item", instance.getStack().getItem().getRegistryName().toString());
            return jsonObject;
        }
    }
}

