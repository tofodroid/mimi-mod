package io.github.tofodroid.mods.mimi.common.loot;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import java.util.List;

public class GenericAddDropLootModifier extends LootModifier {

    private final ItemStack stack;

    protected GenericAddDropLootModifier(LootItemCondition[] conditionsIn, ItemStack itemStack) {
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
        public GenericAddDropLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
            Item toAdd = ShapedRecipe.itemFromJson(object.getAsJsonObject("item"));
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

