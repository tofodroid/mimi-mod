package io.github.tofodroid.mods.mimi.forge.common.loot;

import com.mojang.serialization.Codec;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MIMIMod.MODID);
    public static final RegistryObject<Codec<GenericAddDropLootModifier>> GENERIC_ADD_DROP = REGISTER.register("generic_add_drop", () -> GenericAddDropLootModifier.CODEC);
}