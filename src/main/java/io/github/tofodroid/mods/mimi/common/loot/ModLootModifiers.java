package io.github.tofodroid.mods.mimi.common.loot;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModLootModifiers {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> REGISTER = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, MIMIMod.MODID);
    public static final RegistryObject<GlobalLootModifierSerializer<GenericAddDropLootModifier>> GENERIC_ADD_DROP = REGISTER.register("generic_add_drop", GenericAddDropLootModifier.Serializer::new);
}